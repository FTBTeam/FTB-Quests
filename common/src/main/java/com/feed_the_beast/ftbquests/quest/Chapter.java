package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.ftbquests.util.OrderedCompoundTag;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public final class Chapter extends QuestObject
{
	public final QuestFile file;
	public final ChapterGroup group;
	public String filename;
	public final List<Quest> quests;
	public final List<String> subtitle;
	public boolean alwaysInvisible;
	public String defaultQuestShape;
	public final List<ChapterImage> images;

	public Chapter(QuestFile f, ChapterGroup g)
	{
		file = f;
		group = g;
		filename = "";
		quests = new ArrayList<>();
		subtitle = new ArrayList<>(0);
		alwaysInvisible = false;
		defaultQuestShape = "";
		images = new ArrayList<>();
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

		nbt.putString("default_quest_shape", defaultQuestShape);

		if (!images.isEmpty())
		{
			ListTag list = new ListTag();

			for (ChapterImage image : images)
			{
				CompoundTag nbt1 = new OrderedCompoundTag();
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
		super.readData(nbt);
		subtitle.clear();

		ListTag subtitleNBT = nbt.getList("subtitle", NbtType.STRING);

		for (int i = 0; i < subtitleNBT.size(); i++)
		{
			subtitle.add(subtitleNBT.getString(i));
		}

		alwaysInvisible = nbt.getBoolean("always_invisible");
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
		defaultQuestShape = buffer.readUtf(Short.MAX_VALUE);
		NetUtils.read(buffer, images, d -> {
			ChapterImage image = new ChapterImage(this);
			image.readNetData(d);
			return image;
		});
	}

	public int getIndex()
	{
		return group.chapters.indexOf(this);
	}

	@Override
	public int getRelativeProgressFromChildren(PlayerData data)
	{
		if (alwaysInvisible)
		{
			return 100;
		}

		if (quests.isEmpty())
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

		for (ChapterGroup g : file.chapterGroups)
		{
			for (Chapter chapter : g.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.dependencies.contains(this))
					{
						data.checkAutoCompletion(quest);
					}
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
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("ftbquests.unnamed");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (Quest quest : quests)
		{
			list.add(quest.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		group.chapters.remove(this);
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

			Set<String> existingNames = group.chapters.stream().map(ch -> ch.filename).collect(Collectors.toSet());
			int i = 2;

			while (existingNames.contains(filename))
			{
				filename = s + "_" + i;
				i++;
			}
		}

		group.chapters.add(this);

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

	public String getFilename()
	{
		if (filename.isEmpty())
		{
			filename = getCodeString(this);
		}

		return filename;
	}

	@Override
	public String getPath()
	{
		return "chapters/" + getFilename() + ".snbt";
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("subtitle", subtitle, new ConfigString(null), "");
		config.addBool("always_invisible", alwaysInvisible, v -> alwaysInvisible = v, false);
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
	}

	@Override
	protected void verifyDependenciesInternal(long original, int depth)
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
		return !group.isDefaultGroup();
	}

	public String getDefaultQuestShape()
	{
		return defaultQuestShape.isEmpty() ? file.getDefaultQuestShape() : defaultQuestShape;
	}
}