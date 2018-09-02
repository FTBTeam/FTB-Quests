package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class DependencyTask extends QuestTask
{
	public static class TaskDep extends DependencyTask
	{
		public TaskDep(Quest quest, NBTTagCompound nbt)
		{
			super(quest, nbt);
		}

		@Override
		public QuestObjectType getType()
		{
			return QuestObjectType.TASK;
		}
	}

	public static class QuestDep extends DependencyTask
	{
		public QuestDep(Quest quest, NBTTagCompound nbt)
		{
			super(quest, nbt);
		}

		@Override
		public QuestObjectType getType()
		{
			return QuestObjectType.QUEST;
		}
	}

	public static class ChapterDep extends DependencyTask
	{
		public ChapterDep(Quest quest, NBTTagCompound nbt)
		{
			super(quest, nbt);
		}

		@Override
		public QuestObjectType getType()
		{
			return QuestObjectType.CHAPTER;
		}
	}

	public static class VariableDep extends DependencyTask
	{
		public VariableDep(Quest quest, NBTTagCompound nbt)
		{
			super(quest, nbt);
		}

		@Override
		public QuestObjectType getType()
		{
			return QuestObjectType.VARIABLE;
		}
	}

	public String objectId;
	private QuestObject cachedDep;

	public DependencyTask(Quest quest, NBTTagCompound nbt)
	{
		super(quest);
		objectId = nbt.getString("object");
	}

	public abstract QuestObjectType getType();

	@Override
	@Nullable
	public QuestObject getDependency()
	{
		if (cachedDep == null)
		{
			cachedDep = quest.chapter.file.getQuest(objectId);
		}

		return cachedDep;
	}

	@Override
	public long getMaxProgress()
	{
		return 1L;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("object", objectId);
	}

	@Override
	public Icon getAltIcon()
	{
		QuestObject object = getDependency();
		return object == null ? GuiIcons.INFO : object.getIcon();
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		QuestObject object = getDependency();
		return new TextComponentTranslation("ftbquests.dependency").appendText(": ").appendSibling(object == null ? new TextComponentTranslation(getType().getTranslationKey()) : object.getDisplayName());
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("object", new ConfigQuestObject(objectId)
		{
			@Override
			public String getString()
			{
				return objectId;
			}

			@Override
			public void setString(String v)
			{
				objectId = v;
			}
		}.addType(getType()), new ConfigQuestObject("")).setDisplayName(new TextComponentTranslation(getType().getTranslationKey()));
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends QuestTaskData<DependencyTask>
	{
		private Data(DependencyTask task, ITeamData data)
		{
			super(task, data);
		}

		@Nullable
		@Override
		public NBTBase toNBT()
		{
			return null;
		}

		@Override
		public void fromNBT(@Nullable NBTBase nbt)
		{
		}

		@Override
		public long getProgress()
		{
			QuestObject object = task.getDependency();

			if (object != null)
			{
				try
				{
					if (object.isComplete(teamData))
					{
						return 1L;
					}
				}
				catch (StackOverflowError error)
				{
					FTBQuests.LOGGER.error("Dependency loop in " + task.getID() + " (ID erroring: " + task.objectId + ")!");
					task.objectId = "";
					task.cachedDep = null;
				}
			}

			return 0L;
		}

		@Override
		public void resetProgress()
		{
			QuestObject object = task.getDependency();

			if (object != null)
			{
				object.resetProgress(teamData);
			}
		}

		@Override
		public void completeInstantly()
		{
			QuestObject object = task.getDependency();

			if (object != null)
			{
				object.completeInstantly(teamData);
			}
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
		{
			return false;
		}

		@Nullable
		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
		{
			return null;
		}
	}
}