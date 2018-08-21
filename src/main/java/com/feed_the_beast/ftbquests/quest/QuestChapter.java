package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class QuestChapter extends ProgressingQuestObject
{
	public final QuestFile file;
	public int index;
	public final List<Quest> quests;
	public final List<String> description;
	public final List<String> dependencies;

	public QuestChapter(QuestFile f, NBTTagCompound nbt)
	{
		file = f;
		title = nbt.getString("title");
		description = new ArrayList<>();
		icon = QuestFile.readIcon(nbt, "icon");
		quests = new ArrayList<>();

		NBTTagList desc = nbt.getTagList("description", Constants.NBT.TAG_STRING);

		for (int i = 0; i < desc.tagCount(); i++)
		{
			description.add(desc.getStringTagAt(i));
		}

		dependencies = new ArrayList<>();

		NBTTagList depList = nbt.getTagList("dependencies", Constants.NBT.TAG_STRING);

		for (int i = 0; i < depList.tagCount(); i++)
		{
			dependencies.add(depList.getStringTagAt(i));
		}

		readID(nbt);

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
		nbt.setString("id", id);

		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		QuestFile.writeIcon(nbt, "icon", icon);

		if (!description.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (String v : description)
			{
				list.appendTag(new NBTTagString(v));
			}

			nbt.setTag("description", list);
		}

		if (!dependencies.isEmpty())
		{
			NBTTagList depList = new NBTTagList();

			for (String value : dependencies)
			{
				depList.appendTag(new NBTTagString(value));
			}

			nbt.setTag("dependencies", depList);
		}

		if (!quests.isEmpty())
		{
			NBTTagList questsList = new NBTTagList();

			for (Quest quest : quests)
			{
				NBTTagCompound questNBT = new NBTTagCompound();
				quest.writeData(questNBT);
				questsList.appendTag(questNBT);
			}

			nbt.setTag("quests", questsList);
		}
	}

	@Override
	public long getProgress(IProgressData data)
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
	public double getRelativeProgress(IProgressData data)
	{
		double progress = 0D;

		for (Quest quest : quests)
		{
			progress += quest.getRelativeProgress(data);
		}

		return progress / (double) quests.size();
	}

	@Override
	public boolean isComplete(IProgressData data)
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
	public void resetProgress(IProgressData data)
	{
		for (Quest quest : quests)
		{
			quest.resetProgress(data);
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
		return new TextComponentString("");
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

		group.add("dependencies", new ConfigList<ConfigString>(new ConfigString(""))
		{
			@Override
			public void readFromList()
			{
				dependencies.clear();

				for (ConfigString value : list)
				{
					dependencies.add(value.getString());
				}
			}

			@Override
			public void writeToList()
			{
				list.clear();

				for (String value : dependencies)
				{
					list.add(new ConfigString(value));
				}
			}
		}, new ConfigList<>(new ConfigString("")));
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