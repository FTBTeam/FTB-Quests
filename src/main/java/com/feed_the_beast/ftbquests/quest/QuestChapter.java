package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends QuestObject
{
	public final QuestFile file;
	public int chapterIndex;
	public EnumQuestVisibilityType visibilityType;
	public final List<Quest> quests;
	public final List<String> description;

	public QuestChapter(QuestFile f, NBTTagCompound nbt)
	{
		file = f;
		readCommonData(nbt);
		description = new ArrayList<>();
		visibilityType = EnumQuestVisibilityType.NAME_MAP.get(nbt.getString("visibility"));
		quests = new ArrayList<>();

		NBTTagList desc = nbt.getTagList("description", Constants.NBT.TAG_STRING);

		for (int i = 0; i < desc.tagCount(); i++)
		{
			description.add(desc.getStringTagAt(i));
		}

		NBTTagList questsList = nbt.getTagList("quests", Constants.NBT.TAG_COMPOUND);

		for (int j = 0; j < questsList.tagCount(); j++)
		{
			quests.add(new Quest(this, questsList.getCompoundTagAt(j)));
		}
	}

	@Override
	public QuestFile getQuestFile()
	{
		return file;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.CHAPTER;
	}

	@Override
	public String getID()
	{
		return id;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		writeCommonData(nbt);

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

		if (!quests.isEmpty())
		{
			NBTTagList questsList = new NBTTagList();

			for (Quest quest : quests)
			{
				NBTTagCompound questNBT = new NBTTagCompound();
				quest.writeData(questNBT);
				questNBT.setString("id", quest.id);
				questsList.appendTag(questNBT);
			}

			nbt.setTag("quests", questsList);
		}
	}

	@Override
	public long getProgress(ITeamData data)
	{
		long progress = 0L;

		for (Quest quest : quests)
		{
			progress += quest.getProgress(data);
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (Quest quest : quests)
		{
			maxProgress += quest.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		for (Quest quest : quests)
		{
			progress += quest.getRelativeProgress(data);
		}

		return fixRelativeProgress(progress, quests.size());
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		for (Quest quest : quests)
		{
			if (!quest.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data)
	{
		super.onCompleted(data);
		new ObjectCompletedEvent.ChapterEvent(data, this).post();

		if (file.isComplete(data))
		{
			file.onCompleted(data);
		}
	}

	@Override
	public void resetProgress(ITeamData data)
	{
		for (Quest quest : quests)
		{
			quest.resetProgress(data);
		}
	}

	@Override
	public void completeInstantly(ITeamData data)
	{
		for (Quest quest : quests)
		{
			quest.completeInstantly(data);
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
	public void getConfig(ConfigGroup group)
	{
		group.add("description", new ConfigList<ConfigString>(new ConfigString(""))
		{
			@Override
			public void readFromList()
			{
				description.clear();

				for (ConfigString value : list)
				{
					description.add(value.getString());
				}
			}

			@Override
			public void writeToList()
			{
				list.clear();

				for (String value : description)
				{
					list.add(new ConfigString(value));
				}
			}
		}, new ConfigList<>(new ConfigString("")));

		group.add("visibility", new ConfigEnum<EnumQuestVisibilityType>(EnumQuestVisibilityType.NAME_MAP)
		{
			@Override
			public EnumQuestVisibilityType getValue()
			{
				return visibilityType;
			}

			@Override
			public void setValue(EnumQuestVisibilityType v)
			{
				visibilityType = v;
			}
		}, new ConfigEnum<>(EnumQuestVisibilityType.NAME_MAP));
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
}