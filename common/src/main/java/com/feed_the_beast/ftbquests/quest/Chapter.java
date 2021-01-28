package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.ftbquests.util.OrderedCompoundNBT;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigString;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public final class Chapter extends QuestObject
{
	public final QuestFile file;
	public String filename;
	public final List<Quest> quests;
	public final List<String> subtitle;
	public boolean alwaysInvisible;
	public Chapter group;
	public String defaultQuestShape;
	public final List<ChapterImage> images;
	public int orderIndex;

	public Chapter(QuestFile f)
	{
		file = f;
		filename = "";
		quests = new ArrayList<>();
		subtitle = new ArrayList<>(0);
		alwaysInvisible = false;
		group = null;
		defaultQuestShape = "";
		images = new ArrayList<>();
		orderIndex = 0;
	}

	@Override
	public String toString()
	{
		return filename;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.CHAPTER;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return file;
	}

	@Override
	public Chapter getQuestChapter()
	{
		return this;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		nbt.putString("filename", filename);
		nbt.putInt("order_index", orderIndex);
		super.writeData(nbt);

		if (!subtitle.isEmpty())
		{
			ListTag list = new ListTag();

			for (String v : subtitle)
			{
				list.add(StringTag.valueOf(v));
			}

			nbt.put("subtitle", list);
		}

		if (alwaysInvisible)
		{
			nbt.putBoolean("always_invisible", true);
		}

		if (group != null && !group.invalid)
		{
			nbt.putInt("group", group.id);
		}

		nbt.putString("default_quest_shape", defaultQuestShape);

		if (!images.isEmpty())
		{
			ListTag list = new ListTag();

			for (ChapterImage image : images)
			{
				CompoundTag nbt1 = new OrderedCompoundNBT();
				image.writeData(nbt1);
				list.add(nbt1);
			}

			nbt.put("images", list);
		}
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		filename = nbt.getString("filename");
		orderIndex = nbt.getInt("order_index");
		super.readData(nbt);
		subtitle.clear();

		ListTag subtitleNBT = nbt.getList("subtitle", NbtType.STRING);

		for (int i = 0; i < subtitleNBT.size(); i++)
		{
			subtitle.add(subtitleNBT.getString(i));
		}

		alwaysInvisible = nbt.getBoolean("always_invisible");
		group = file.getChapter(nbt.getInt("group"));
		defaultQuestShape = nbt.getString("default_quest_shape");

		if (defaultQuestShape.equals("default"))
		{
			defaultQuestShape = "";
		}

		ListTag imgs = nbt.getList("images", NbtType.COMPOUND);

		images.clear();

		for (int i = 0; i < imgs.size(); i++)
		{
			ChapterImage image = new ChapterImage(this);
			image.readData(imgs.getCompound(i));
			images.add(image);
		}
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeUtf(filename, Short.MAX_VALUE);
		NetUtils.writeStrings(buffer, subtitle);
		buffer.writeBoolean(alwaysInvisible);
		buffer.writeInt(group == null || group.invalid ? 0 : group.id);
		buffer.writeUtf(defaultQuestShape, Short.MAX_VALUE);
		NetUtils.write(buffer, images, (d, img) -> img.writeNetData(d));
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		filename = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.readStrings(buffer, subtitle);
		alwaysInvisible = buffer.readBoolean();
		group = file.getChapter(buffer.readInt());
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.read(buffer, images, d -> {
			ChapterImage image = new ChapterImage(this);
			image.readNetData(d);
			return image;
		});
	}

	public int getIndex()
	{
		return file.chapters.indexOf(this);
	}

	@Override
	public int getRelativeProgressFromChildren(PlayerData data)
	{
		if (alwaysInvisible)
		{
			return 100;
		}

		List<Chapter> children = getChildren();

		if (quests.isEmpty() && children.isEmpty())
		{
			return 100;
		}

		int progress = 0;
		int count = 0;

		for (Quest quest : quests)
		{
			if (!quest.isProgressionIgnored())
			{
				progress += data.getRelativeProgress(quest);
				count++;
			}
		}

		for (Chapter chapter : getChildren())
		{
			progress += chapter.getRelativeProgressFromChildren(data);
			count++;
		}

		if (count <= 0)
		{
			return 100;
		}

		return getRelativeProgressFromChildren(progress, count);
	}

	@Override
	public void onCompleted(PlayerData data, List<ServerPlayer> onlineMembers, List<ServerPlayer> notifiedPlayers)
	{
		super.onCompleted(data, onlineMembers, notifiedPlayers);
		ObjectCompletedEvent.CHAPTER.invoker().act(new ObjectCompletedEvent.ChapterEvent(data, this, onlineMembers, notifiedPlayers));

		if (!disableToast)
		{
			for (ServerPlayer player : notifiedPlayers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		for (Chapter chapter : file.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (quest.dependencies.contains(this))
				{
					data.checkAutoCompletion(quest);
				}
			}
		}

		if (data.isComplete(file))
		{
			file.onCompleted(data, onlineMembers, notifiedPlayers);
		}
	}

	@Override
	public void changeProgress(PlayerData data, ChangeProgress type)
	{
		for (Quest quest : quests)
		{
			quest.changeProgress(data, type);
		}

		for (Chapter chapter : getChildren())
		{
			chapter.changeProgress(data, type);
		}
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (Quest quest : quests)
		{
			list.add(quest.getIcon());
		}

		for (Chapter child : getChildren())
		{
			list.add(child.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		file.chapters.remove(this);
	}

	@Override
	public void deleteChildren()
	{
		for (Quest quest : quests)
		{
			quest.deleteChildren();
			quest.invalid = true;
		}

		quests.clear();
	}

	@Override
	public void onCreated()
	{
		if (filename.isEmpty())
		{
			String s = title.replace(' ', '_').replaceAll("\\W", "").toLowerCase().trim();

			if (s.isEmpty())
			{
				s = toString();
			}

			filename = s;

			Set<String> existingNames = file.chapters.stream().map(ch -> ch.filename).collect(Collectors.toSet());
			int i = 2;

			while (existingNames.contains(filename))
			{
				filename = s + "_" + i;
				i++;
			}
		}

		file.chapters.add(this);

		if (!quests.isEmpty())
		{
			List<Quest> l = new ArrayList<>(quests);
			quests.clear();
			for (Quest quest : l)
			{
				quest.onCreated();
			}
		}
	}

	@Override
	public String getPath()
	{
		return "chapters/" + filename + ".snbt";
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("subtitle", subtitle, new ConfigString(null), "");
		config.addBool("always_invisible", alwaysInvisible, v -> alwaysInvisible = v, false);

		//Predicate<QuestObjectBase> predicate = object -> object == null || (object instanceof Chapter && object != this && ((Chapter) object).group == null);
		//config.add("group", new ConfigQuestObject<>(predicate), group, v -> group = v, null);

		config.addEnum("default_quest_shape", defaultQuestShape.isEmpty() ? "default" : defaultQuestShape, v -> defaultQuestShape = v.equals("default") ? "" : v, QuestShape.idMapWithDefault);
	}

	@Override
	public boolean isVisible(PlayerData data)
	{
		if (alwaysInvisible)
		{
			return false;
		}

		for (Quest quest : quests)
		{
			if (quest.isVisible(data))
			{
				return true;
			}
		}

		for (Chapter child : getChildren())
		{
			if (child.isVisible(data))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (Quest quest : quests)
		{
			quest.clearCachedData();
		}

		for (Chapter chapter : getChildren())
		{
			chapter.clearCachedData();
		}
	}

	public boolean hasChildren()
	{
		if (group != null)
		{
			return false;
		}

		for (Chapter chapter : file.chapters)
		{
			if (chapter.group == this)
			{
				return true;
			}
		}

		return false;
	}

	public List<Chapter> getChildren()
	{
		List<Chapter> list = Collections.emptyList();

		if (group != null)
		{
			return list;
		}

		for (Chapter chapter : file.chapters)
		{
			if (chapter.group == this)
			{
				if (list.isEmpty())
				{
					list = new ArrayList<>(3);
				}

				list.add(chapter);
			}
		}

		return list;
	}

	@Override
	protected void verifyDependenciesInternal(int original, int depth)
	{
		if (depth >= 1000)
		{
			throw new DependencyDepthException(this);
		}

		for (Quest quest : quests)
		{
			if (quest.id == original)
			{
				throw new DependencyLoopException(this);
			}

			quest.verifyDependenciesInternal(original, depth + 1);
		}
	}

	public boolean hasGroup()
	{
		return group != null && !group.invalid;
	}

	public String getDefaultQuestShape()
	{
		return defaultQuestShape.isEmpty() ? file.getDefaultQuestShape() : defaultQuestShape;
	}
}