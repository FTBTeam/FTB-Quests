package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.quest.widget.QuestWidget;
import com.feed_the_beast.ftbquests.quest.widget.QuestWidgetType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends QuestObject
{
	public final QuestFile file;
	public EnumQuestVisibilityType visibilityType;
	public final List<Quest> quests;
	public final List<String> description;
	public final List<QuestWidget> widgets;

	public QuestChapter(QuestFile f)
	{
		file = f;
		description = new ArrayList<>();
		visibilityType = EnumQuestVisibilityType.NORMAL;
		quests = new ArrayList<>();
		widgets = new ArrayList<>();
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
	public QuestChapter getQuestChapter()
	{
		return this;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			nbt.setString("type", visibilityType.getName());
		}

		if (!description.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (String v : description)
			{
				list.appendTag(new NBTTagString(v));
			}

			nbt.setTag("description", list);
		}

		if (!widgets.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (QuestWidget w : widgets)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				w.writeData(nbt1);
				nbt1.setString("type", w.getType().getID());
				nbt1.setIntArray("location", new int[] {w.x, w.y, w.w, w.h});
				list.appendTag(nbt1);
			}

			nbt.setTag("widgets", list);
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		description.clear();
		visibilityType = EnumQuestVisibilityType.NAME_MAP.get(nbt.getString("visibility"));

		NBTTagList desc = nbt.getTagList("description", Constants.NBT.TAG_STRING);

		for (int i = 0; i < desc.tagCount(); i++)
		{
			description.add(desc.getStringTagAt(i));
		}

		NBTTagList wlist = nbt.getTagList("widgets", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < wlist.tagCount(); i++)
		{
			nbt = wlist.getCompoundTagAt(i);
			int[] loc = nbt.getIntArray("location");

			if (loc.length == 4)
			{
				QuestWidget widget = QuestWidgetType.NAME_MAP.get(nbt.getString("type")).supplier.get();
				widget.x = loc[0];
				widget.y = loc[1];
				widget.w = loc[2];
				widget.h = loc[3];
				widget.readData(nbt);
				widgets.add(widget);
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.write(visibilityType, EnumQuestVisibilityType.NAME_MAP);
		data.writeCollection(description, DataOut.STRING);
		data.writeVarInt(widgets.size());

		for (QuestWidget w : widgets)
		{
			data.write(w.getType(), QuestWidgetType.NAME_MAP);
			data.writeVarInt(w.x);
			data.writeVarInt(w.y);
			data.writeVarInt(w.w);
			data.writeVarInt(w.h);
			w.writeNetData(data);
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		visibilityType = data.read(EnumQuestVisibilityType.NAME_MAP);
		data.readCollection(description, DataIn.STRING);
		int s = data.readVarInt();
		widgets.clear();

		for (int i = 0; i < s; i++)
		{
			QuestWidget w = data.read(QuestWidgetType.NAME_MAP).supplier.get();
			w.x = data.readVarInt();
			w.y = data.readVarInt();
			w.w = data.readVarInt();
			w.h = data.readVarInt();
			w.readNetData(data);
			widgets.add(w);
		}
	}

	public int getIndex()
	{
		return file.chapters.indexOf(this);
	}

	@Override
	public long getProgress(ITeamData data)
	{
		long progress = 0L;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				progress += quest.getProgress(data);
			}
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				maxProgress += quest.getMaxProgress();
			}
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		for (Quest quest : quests)
		{
			if (!quest.canRepeat)
			{
				progress += quest.getRelativeProgress(data);
			}
		}

		return fixRelativeProgress(progress, quests.size());
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		for (Quest quest : quests)
		{
			if (!quest.canRepeat && !quest.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data, List<EntityPlayerMP> onlineMembers)
	{
		super.onCompleted(data, onlineMembers);
		new ObjectCompletedEvent.ChapterEvent(data, this).post();

		for (EntityPlayerMP player : onlineMembers)
		{
			new MessageDisplayCompletionToast(id).sendTo(player);
		}

		if (file.isComplete(data))
		{
			file.onCompleted(data, onlineMembers);
		}
	}

	@Override
	public void resetProgress(ITeamData data, boolean dependencies)
	{
		for (Quest quest : quests)
		{
			quest.resetProgress(data, dependencies);
		}
	}

	@Override
	public void completeInstantly(ITeamData data, boolean dependencies)
	{
		for (Quest quest : quests)
		{
			quest.completeInstantly(data, dependencies);
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

		return IconAnimation.fromList(list, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		return new TextComponentTranslation("ftbquests.unnamed");
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
		file.chapters.add(this);

		if (!quests.isEmpty())
		{
			for (Quest quest : ListUtils.clearAndCopy(quests))
			{
				quest.onCreated();
			}
		}
	}

	@Override
	public File getFile(File folder)
	{
		return new File(folder, "chapters/" + getCodeString());
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addList("description", description, new ConfigString(""), ConfigString::new, ConfigString::getString);
		//config.addEnum("visibility", () -> visibilityType, v -> visibilityType = v, EnumQuestVisibilityType.NAME_MAP);
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		for (Quest quest : quests)
		{
			v = v.weakest(quest.getVisibility(data));

			if (v.isInvisible())
			{
				break;
			}
		}

		return v;
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

	@Nullable
	public Quest getQuestAt(byte x, byte y)
	{
		for (Quest quest : quests)
		{
			if (quest.x == x && quest.y == y)
			{
				return quest;
			}
		}

		return null;
	}
}