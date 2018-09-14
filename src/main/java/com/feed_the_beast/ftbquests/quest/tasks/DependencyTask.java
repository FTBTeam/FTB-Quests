package com.feed_the_beast.ftbquests.quest.tasks;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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

		@Override
		@SideOnly(Side.CLIENT)
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);

			if (getDependency() instanceof QuestTask)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.task") + ": " + QuestObjectType.TASK.getColor() + getDependency().getDisplayName().getUnformattedText());
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + QuestObjectType.QUEST.getColor() + ((QuestTask) getDependency()).quest.getDisplayName().getUnformattedText());
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + ((QuestTask) getDependency()).quest.chapter.getDisplayName().getUnformattedText());
			}
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

		@Override
		@SideOnly(Side.CLIENT)
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);

			if (getDependency() instanceof Quest)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.quest") + ": " + QuestObjectType.QUEST.getColor() + getDependency().getDisplayName().getUnformattedText());
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + ((Quest) getDependency()).chapter.getDisplayName().getUnformattedText());
			}
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

		@Override
		@SideOnly(Side.CLIENT)
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);

			if (getDependency() instanceof QuestChapter)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.chapter") + ": " + QuestObjectType.CHAPTER.getColor() + getDependency().getDisplayName().getUnformattedText());
			}
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

		@Override
		@SideOnly(Side.CLIENT)
		public void addMouseOverText(List<String> list)
		{
			super.addMouseOverText(list);

			if (getDependency() instanceof QuestChapter)
			{
				list.add(TextFormatting.GRAY + I18n.format("ftbquests.variable") + ": " + QuestObjectType.VARIABLE.getColor() + getDependency().getDisplayName().getUnformattedText());
			}
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
		if (objectId.isEmpty())
		{
			return null;
		}
		else if (cachedDep == null || cachedDep.invalid)
		{
			cachedDep = quest.chapter.file.get(objectId);
		}

		if (cachedDep == null)
		{
			FTBQuests.LOGGER.warn("Removed dependency '" + getID() + "' with missing ID '" + objectId + "'");
			quest.chapter.file.deleteObject(getID());
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
		if (cachedDep != null)
		{
			objectId = cachedDep.getID();
		}

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
		if (cachedDep != null)
		{
			objectId = cachedDep.getID();
		}

		group.add("object", new ConfigQuestObject("")
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
	@SideOnly(Side.CLIENT)
	public void addMouseOverText(List<String> list)
	{
		if (cachedDep != null)
		{
			objectId = cachedDep.getID();
		}

		list.add(TextFormatting.DARK_GRAY + objectId);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked()
	{
		ClientQuestFile.INSTANCE.questTreeGui.open(getDependency());
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
					FTBQuests.LOGGER.error("Removed looping dependency '" + task.getID() + "' with erroring ID '" + task.objectId + "'");
					task.quest.chapter.file.deleteObject(task.getID());
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