package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.io.Bits;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.ListUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.MessageDisplayCompletionToast;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject
{
	public static final int POS_LIMIT = 25;
	private static final List<QuestObjectType> DEP_TYPES = Arrays.asList(QuestObjectType.QUEST, QuestObjectType.CHAPTER, QuestObjectType.VARIABLE);

	public final QuestChapter chapter;
	public String description;
	public byte x, y;
	public EnumQuestShape shape;
	public final List<String> text;
	public final List<Dependency> dependencies;
	public boolean canRepeat;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	public boolean tasksIgnoreDependencies;
	public String guidePage;
	public String customClick;

	public Quest(QuestChapter c)
	{
		chapter = c;
		description = "";
		x = 0;
		y = 0;
		shape = chapter.file.defaultShape;
		text = new ArrayList<>();
		canRepeat = false;
		dependencies = new ArrayList<>(0);
		tasks = new ArrayList<>(1);
		rewards = new ArrayList<>(1);
		tasksIgnoreDependencies = false;
		guidePage = "";
		customClick = "";
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.QUEST;
	}

	@Override
	public QuestFile getQuestFile()
	{
		return chapter.file;
	}

	@Override
	public QuestChapter getQuestChapter()
	{
		return chapter;
	}

	@Override
	public int getParentID()
	{
		return chapter.id;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		super.writeData(nbt);

		if (x != 0)
		{
			nbt.setByte("x", x);
		}

		if (y != 0)
		{
			nbt.setByte("y", y);
		}

		if (shape != chapter.file.defaultShape)
		{
			nbt.setString("shape", shape.getID());
		}

		if (!description.isEmpty())
		{
			nbt.setString("description", description);
		}

		if (!text.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (String value : text)
			{
				array.appendTag(new NBTTagString(value));
			}

			nbt.setTag("text", array);
		}

		if (canRepeat)
		{
			nbt.setBoolean("can_repeat", true);
		}

		if (tasksIgnoreDependencies)
		{
			nbt.setBoolean("tasks_ignore_deps", true);
		}

		if (!guidePage.isEmpty())
		{
			nbt.setString("guide_page", guidePage);
		}

		if (!customClick.isEmpty())
		{
			nbt.setString("custom_click", customClick);
		}

		dependencies.removeIf(Dependency.PREDICATE_INVALID);

		if (!dependencies.isEmpty())
		{
			boolean allRequired = true;

			for (Dependency dependency : dependencies)
			{
				if (dependency.type != EnumDependencyType.REQUIRED)
				{
					allRequired = false;
					break;
				}
			}

			if (allRequired)
			{
				int[] ai = new int[dependencies.size()];

				for (int i = 0; i < dependencies.size(); i++)
				{
					ai[i] = dependencies.get(i).object.id;
				}

				if (ai.length == 1)
				{
					nbt.setInteger("dependency", ai[0]);
				}
				else
				{
					nbt.setIntArray("dependencies", ai);
				}
			}
			else
			{
				NBTTagList list = new NBTTagList();

				for (Dependency dependency : dependencies)
				{
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setInteger("id", dependency.object.id);

					if (dependency.type != EnumDependencyType.REQUIRED)
					{
						nbt1.setString("type", dependency.type.getID());
					}

					list.appendTag(nbt1);
				}

				nbt.setTag("dependencies", list);
			}
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		description = nbt.getString("description");
		x = (byte) MathHelper.clamp(nbt.getByte("x"), -POS_LIMIT, POS_LIMIT);
		y = (byte) MathHelper.clamp(nbt.getByte("y"), -POS_LIMIT, POS_LIMIT);
		shape = nbt.hasKey("shape") ? EnumQuestShape.NAME_MAP.get(nbt.getString("shape")) : chapter.file.defaultShape;
		text.clear();

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(list.getStringTagAt(k));
		}

		canRepeat = nbt.getBoolean("can_repeat");
		tasksIgnoreDependencies = nbt.getBoolean("tasks_ignore_deps");
		guidePage = nbt.getString("guide_page");
		customClick = nbt.getString("custom_click");

		dependencies.clear();

		NBTBase depsTag = nbt.getTag("dependencies");

		if (depsTag instanceof NBTTagIntArray)
		{
			for (int i : nbt.getIntArray("dependencies"))
			{
				QuestObject object = chapter.file.get(i);

				if (object != null)
				{
					Dependency dependency = new Dependency();
					dependency.object = object;
					dependency.type = EnumDependencyType.REQUIRED;
					dependencies.add(dependency);
				}
			}
		}
		else if (depsTag instanceof NBTTagList)
		{
			list = (NBTTagList) depsTag;

			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				QuestObject object = chapter.file.get(nbt1.getInteger("id"));

				if (object != null)
				{
					Dependency dependency = new Dependency();
					dependency.object = object;
					dependency.type = EnumDependencyType.NAME_MAP.get(nbt1.getString("type"));
					dependencies.add(dependency);
				}
			}
		}
		else
		{
			QuestObject object = chapter.file.get(nbt.getInteger("dependency"));

			if (object != null)
			{
				Dependency dependency = new Dependency();
				dependency.object = object;
				dependency.type = EnumDependencyType.REQUIRED;
				dependencies.add(dependency);
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, canRepeat);
		flags = Bits.setFlag(flags, 2, tasksIgnoreDependencies);
		flags = Bits.setFlag(flags, 4, !guidePage.isEmpty());
		flags = Bits.setFlag(flags, 8, !description.isEmpty());
		flags = Bits.setFlag(flags, 16, !text.isEmpty());
		flags = Bits.setFlag(flags, 32, !customClick.isEmpty());
		data.writeVarInt(flags);

		if (!description.isEmpty())
		{
			data.writeString(description);
		}

		data.writeByte(x);
		data.writeByte(y);
		data.write(shape, EnumQuestShape.NAME_MAP);

		if (!text.isEmpty())
		{
			data.writeCollection(text, DataOut.STRING);
		}

		if (!guidePage.isEmpty())
		{
			data.writeString(guidePage);
		}

		if (!customClick.isEmpty())
		{
			data.writeString(customClick);
		}

		data.writeVarInt(dependencies.size());

		for (Dependency d : dependencies)
		{
			if (d.isInvalid())
			{
				data.writeInt(0);
			}
			else
			{
				data.writeInt(d.object.id);
				data.write(d.type, EnumDependencyType.NAME_MAP);
			}
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		int flags = data.readVarInt();
		description = Bits.getFlag(flags, 8) ? data.readString() : "";
		x = data.readByte();
		y = data.readByte();
		shape = data.read(EnumQuestShape.NAME_MAP);

		if (Bits.getFlag(flags, 16))
		{
			data.readCollection(text, DataIn.STRING);
		}
		else
		{
			text.clear();
		}

		canRepeat = Bits.getFlag(flags, 1);
		tasksIgnoreDependencies = Bits.getFlag(flags, 2);
		guidePage = Bits.getFlag(flags, 4) ? data.readString() : "";
		customClick = Bits.getFlag(flags, 32) ? data.readString() : "";

		dependencies.clear();
		int d = data.readVarInt();

		for (int i = 0; i < d; i++)
		{
			QuestObject object = chapter.file.get(data.readInt());

			if (object != null)
			{
				Dependency dependency = new Dependency();
				dependency.object = object;
				dependency.type = data.read(EnumDependencyType.NAME_MAP);
				dependencies.add(dependency);
			}
		}
	}

	@Override
	public long getProgress(ITeamData data)
	{
		/*if (data.getTimesCompleted(this) > 0)
		{
			return getMaxProgress();
		}*/

		long progress = 0L;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				progress += task.getProgress(data);
			}
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				maxProgress += task.getMaxProgress();
			}
		}

		return maxProgress;
	}

	@Override
	public int getRelativeProgress(ITeamData data)
	{
		/*if (data.getTimesCompleted(this) > 0)
		{
			return 100;
		}*/

		int progress = 0;

		int s = 0;

		for (QuestTask task : tasks)
		{
			if (!task.invalid)
			{
				progress += task.getRelativeProgress(data);
				s++;
			}
		}

		return fixRelativeProgress(progress, s);
	}

	@Override
	public boolean isComplete(ITeamData data)
	{
		/*if (data.getTimesCompleted(this) > 0)
		{
			return true;
		}*/

		for (QuestTask task : tasks)
		{
			if (!task.invalid && !task.isComplete(data))
			{
				return false;
			}
		}

		for (Dependency dependency : dependencies)
		{
			if (!dependency.isInvalid() && !dependency.type.checkFunction.check(data, dependency.object))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void onCompleted(ITeamData data, List<EntityPlayerMP> onlineMembers)
	{
		//data.setTimesCompleted(this, data.getTimesCompleted(this) + 1);
		super.onCompleted(data, onlineMembers);
		new ObjectCompletedEvent.QuestEvent(data, this).post();

		if (!canRepeat)
		{
			for (EntityPlayerMP player : onlineMembers)
			{
				new MessageDisplayCompletionToast(id).sendTo(player);
			}
		}

		if (chapter.isComplete(data))
		{
			chapter.onCompleted(data, onlineMembers);
		}
	}

	@Override
	public void changeProgress(ITeamData data, EnumChangeProgress type)
	{
		//data.setTimesCompleted(this, -1);

		if (type.dependencies)
		{
			for (Dependency dependency : dependencies)
			{
				if (!dependency.isInvalid())
				{
					dependency.object.changeProgress(data, type);
				}
			}
		}

		for (QuestTask task : tasks)
		{
			task.changeProgress(data, type);
		}

		if (type == EnumChangeProgress.RESET || type == EnumChangeProgress.RESET_DEPS)
		{
			data.unclaimRewards(rewards);
		}
	}

	public boolean canStartTasks(ITeamData data)
	{
		if (!tasksIgnoreDependencies)
		{
			for (Dependency dependency : dependencies)
			{
				if (!dependency.object.isComplete(data))
				{
					return false;
				}
			}
		}

		return getVisibility(data).isVisible();
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (QuestTask task : tasks)
		{
			list.add(task.getIcon());
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		if (!tasks.isEmpty())
		{
			return tasks.get(0).getDisplayName().createCopy();
		}

		return new TextComponentTranslation("ftbquests.unnamed");
	}

	@Override
	public void deleteSelf()
	{
		super.deleteSelf();
		chapter.quests.remove(this);
	}

	@Override
	public void deleteChildren()
	{
		for (QuestTask task : tasks)
		{
			task.deleteChildren();
			task.invalid = true;
		}

		for (QuestReward reward : rewards)
		{
			reward.deleteChildren();
			reward.invalid = true;
		}

		tasks.clear();
		rewards.clear();
	}

	@Override
	public void onCreated()
	{
		chapter.quests.add(this);

		if (!tasks.isEmpty())
		{
			for (QuestTask task : ListUtils.clearAndCopy(tasks))
			{
				task.onCreated();
			}
		}
	}

	@Override
	public File getFile(File folder)
	{
		return new File(folder, "chapters/" + chapter.getCodeString() + "/" + getCodeString() + ".nbt");
	}

	@Override
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("x", () -> x, v -> x = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addInt("y", () -> y, v -> y = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addEnum("shape", () -> shape, v -> shape = v, EnumQuestShape.NAME_MAP);
		config.addString("description", () -> description, v -> description = v, "");
		config.addList("text", text, new ConfigString(""), ConfigString::new, ConfigString::getString);
		config.addBool("can_repeat", () -> canRepeat, v -> canRepeat = v, false);
		config.addBool("tasks_ignore_dependencies", () -> tasksIgnoreDependencies, v -> tasksIgnoreDependencies = v, false);
		config.addString("guide_page", () -> guidePage, v -> guidePage = v, "");
		config.addString("custom_click", () -> customClick, v -> customClick = v, "");
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		/*
		for (QuestObject object : dependencies)
		{
			if (object instanceof Quest)
			{
				v = v.strongest(((Quest) object).getVisibility(data));

				if (v.isInvisible())
				{
					return EnumVisibility.INVISIBLE;
				}
			}
		}

		if (data == null && visibilityType != EnumDependencyType.NORMAL)
		{
			return visibilityType == EnumDependencyType.SECRET_ONE || visibilityType == EnumDependencyType.SECRET_ALL ? EnumVisibility.SECRET : EnumVisibility.INVISIBLE;
		}
		*/

		/*
		switch (getVisibilityType())
		{
			case SECRET_ONE:
				for (String value : dependencies)
				{
					QuestObject object = chapter.file.get(value);

					if (object != null && object.isComplete(data))
					{
						return true;
					}
				}

				return false;
			case INVISIBLE:
				for (String value : dependencies)
				{
					QuestObject object = chapter.file.get(value);

					if (object != null && !object.isComplete(data))
					{
						return false;
					}
				}

				return true;
			default:
				return true;
		}
		*/

		return v;
	}

	public QuestTask getTask(int index)
	{
		if (tasks.isEmpty())
		{
			throw new IllegalStateException("Quest has no tasks!");
		}
		else if (index <= 0)
		{
			return tasks.get(0);
		}
		else if (index >= tasks.size())
		{
			return tasks.get(tasks.size() - 1);
		}

		return tasks.get(index);
	}

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (QuestTask task : tasks)
		{
			task.clearCachedData();
		}

		for (QuestReward reward : rewards)
		{
			reward.clearCachedData();
		}
	}

	public boolean hasDependency(QuestObject object)
	{
		if (object.invalid)
		{
			return false;
		}

		for (Dependency dependency : dependencies)
		{
			if (dependency.object == object && !dependency.isInvalid())
			{
				return true;
			}
		}

		return false;
	}

	public boolean verifyDependencies(boolean autofix)
	{
		dependencies.removeIf(Dependency.PREDICATE_INVALID);

		if (dependencies.isEmpty())
		{
			return true;
		}

		try
		{
			if (verifyDependenciesInternal(this, true))
			{
				return true;
			}
		}
		catch (StackOverflowError error)
		{
			if (autofix)
			{
				FTBQuests.LOGGER.error("Looping dependencies found! Deleting all dependencies for quest " + this);
			}
		}

		if (autofix)
		{
			dependencies.clear();

			if (!chapter.file.isClient())
			{
				ServerQuestFile.INSTANCE.save();
			}
		}

		return false;
	}

	private boolean verifyDependenciesInternal(Quest original, boolean firstLoop)
	{
		if (this == original && !firstLoop)
		{
			return false;
		}

		for (Dependency dependency : dependencies)
		{
			if (dependency.object instanceof Quest && !((Quest) dependency.object).verifyDependenciesInternal(original, false))
			{
				return false;
			}
		}

		return true;
	}

	public void checkRepeatableQuests(ITeamData data, UUID player)
	{
		if (!canRepeat)
		{
			return;
		}

		for (QuestReward reward1 : rewards)
		{
			if (!data.isRewardClaimed(player, reward1))
			{
				return;
			}
		}

		changeProgress(data, EnumChangeProgress.RESET);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void editedFromGUI()
	{
		GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

		if (gui != null)
		{
			gui.quests.refreshWidgets();
			gui.questLeft.refreshWidgets();
			gui.questRight.refreshWidgets();
		}

		if (chapter.quests.size() == 1) //Edge case, need to figure out better way
		{
			ClientQuestFile.INSTANCE.questTreeGui.resetScroll(true);
		}
	}
}