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
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.net.MessageDisplayToast;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.ConfigQuestObject;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	public EnumQuestVisibilityType visibilityType;
	public byte x, y;
	public EnumQuestShape shape;
	public final List<String> text;
	public final Set<QuestObject> dependencies;
	public boolean canRepeat;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	public boolean tasksIgnoreDependencies;

	public Quest(QuestChapter c)
	{
		chapter = c;
		description = "";
		visibilityType = EnumQuestVisibilityType.NORMAL;
		x = 0;
		y = 0;
		shape = chapter.file.defaultShape;
		text = new ArrayList<>();
		canRepeat = false;
		dependencies = new HashSet<>();
		tasks = new ArrayList<>();
		rewards = new ArrayList<>();
		tasksIgnoreDependencies = false;
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

		if (visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			nbt.setString("type", visibilityType.getName());
		}

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

		dependencies.removeIf(QuestObjectBase.PREDICATE_INVALID);

		if (!dependencies.isEmpty())
		{
			if (dependencies.size() == 1)
			{
				nbt.setInteger("dependency", dependencies.iterator().next().id);
			}
			else
			{
				int[] ai = new int[dependencies.size()];
				int i = 0;

				for (QuestObject object : dependencies)
				{
					ai[i] = object.id;
					i++;
				}

				nbt.setIntArray("dependencies", ai);
			}
		}
	}

	@Override
	public void readData(NBTTagCompound nbt)
	{
		super.readData(nbt);
		description = nbt.getString("description");
		visibilityType = EnumQuestVisibilityType.NAME_MAP.get(nbt.getString("visibility"));
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

		dependencies.clear();

		NBTBase deps = nbt.getTag("dependencies");

		if (deps == null)
		{
			deps = nbt.getTag("dependency");
		}

		if (deps instanceof NBTTagList)
		{
			list = (NBTTagList) deps;

			for (int i = 0; i < list.tagCount(); i++)
			{
				QuestObject o = chapter.file.get(chapter.file.getID(list.get(i)));

				if (o != null)
				{
					dependencies.add(o);
				}
			}
		}
		else if (deps instanceof NBTTagIntArray)
		{
			for (int i : ((NBTTagIntArray) deps).getIntArray())
			{
				QuestObject o = chapter.file.get(i);

				if (o != null)
				{
					dependencies.add(o);
				}
			}
		}
		else
		{
			QuestObject o = chapter.file.get(chapter.file.getID(deps));

			if (o != null)
			{
				dependencies.add(o);
			}
		}
	}

	@Override
	public void writeNetData(DataOut data)
	{
		super.writeNetData(data);
		data.writeString(description);
		data.write(visibilityType, EnumQuestVisibilityType.NAME_MAP);
		data.writeByte(x);
		data.writeByte(y);
		data.write(shape, EnumQuestShape.NAME_MAP);
		data.writeCollection(text, DataOut.STRING);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, canRepeat);
		flags = Bits.setFlag(flags, 2, tasksIgnoreDependencies);
		data.writeVarInt(flags);
		data.writeVarInt(dependencies.size());

		for (QuestObject d : dependencies)
		{
			if (d == null || d.invalid)
			{
				data.writeInt(0);
			}
			else
			{
				data.writeInt(d.id);
			}
		}
	}

	@Override
	public void readNetData(DataIn data)
	{
		super.readNetData(data);
		description = data.readString();
		visibilityType = data.read(EnumQuestVisibilityType.NAME_MAP);
		x = data.readByte();
		y = data.readByte();
		shape = data.read(EnumQuestShape.NAME_MAP);
		data.readCollection(text, DataIn.STRING);
		int flags = data.readVarInt();
		canRepeat = Bits.getFlag(flags, 1);
		tasksIgnoreDependencies = Bits.getFlag(flags, 2);

		int d = data.readVarInt();

		for (int i = 0; i < d; i++)
		{
			QuestObject object = chapter.file.get(data.readInt());

			if (object != null)
			{
				dependencies.add(object);
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

		for (QuestObject object : dependencies)
		{
			if (!object.isComplete(data))
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

		for (EntityPlayerMP player : onlineMembers)
		{
			new MessageDisplayToast(id).sendTo(player);
		}

		if (chapter.isComplete(data))
		{
			chapter.onCompleted(data, onlineMembers);
		}
	}

	@Override
	public void resetProgress(ITeamData data, boolean deps)
	{
		//data.setTimesCompleted(this, -1);

		if (deps)
		{
			for (QuestObject dep : dependencies)
			{
				if (!dep.invalid)
				{
					dep.resetProgress(data, true);
				}
			}
		}

		for (QuestTask task : tasks)
		{
			task.resetProgress(data, deps);
		}

		data.unclaimRewards(rewards);
	}

	@Override
	public void completeInstantly(ITeamData data, boolean deps)
	{
		if (deps)
		{
			for (QuestObject dep : dependencies)
			{
				if (!dep.invalid)
				{
					dep.completeInstantly(data, true);
				}
			}
		}

		for (QuestTask task : tasks)
		{
			task.completeInstantly(data, deps);
		}
	}

	public boolean canStartTasks(ITeamData data)
	{
		if (!tasksIgnoreDependencies)
		{
			for (QuestObject object : dependencies)
			{
				if (!object.isComplete(data))
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
		for (QuestTask task : tasks)
		{
			return task.getDisplayName().createCopy();
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
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addInt("x", () -> x, v -> x = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addInt("y", () -> y, v -> y = (byte) v, 0, -POS_LIMIT, POS_LIMIT);
		config.addEnum("shape", () -> shape, v -> shape = v, EnumQuestShape.NAME_MAP);
		//config.addEnum("visibility", () -> visibilityType, v -> visibilityType = v, EnumQuestVisibilityType.NAME_MAP);
		config.addString("description", () -> description, v -> description = v, "");
		config.addList("text", text, new ConfigString(""), ConfigString::new, ConfigString::getString);
		config.addList("dependencies", dependencies, new ConfigQuestObject(chapter.file, null, DEP_TYPES), v -> new ConfigQuestObject(chapter.file, v, DEP_TYPES), c -> (QuestObject) c.getObject()).setDisplayName(new TextComponentTranslation("ftbquests.dependencies"));
		config.addBool("can_repeat", () -> canRepeat, v -> canRepeat = v, false);
		config.addBool("tasks_ignore_dependencies", () -> tasksIgnoreDependencies, v -> tasksIgnoreDependencies = v, false);
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		for (QuestObject object : dependencies)
		{
			if (object instanceof Quest)
			{
				v = v.weakest(((Quest) object).getVisibility(data));

				if (v.isInvisible())
				{
					return EnumVisibility.INVISIBLE;
				}
			}
		}

		if (data == null && visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			return visibilityType == EnumQuestVisibilityType.SECRET_ONE || visibilityType == EnumQuestVisibilityType.SECRET_ALL ? EnumVisibility.SECRET : EnumVisibility.INVISIBLE;
		}

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
		return !object.invalid && dependencies.contains(object);
	}

	public boolean verifyDependencies()
	{
		dependencies.removeIf(QuestObjectBase.PREDICATE_INVALID);

		if (dependencies.isEmpty())
		{
			return true;
		}

		if (verifyDependenciesInternal(this, true))
		{
			return true;
		}

		dependencies.clear();

		if (!chapter.file.isClient())
		{
			ServerQuestFile.INSTANCE.save();
		}

		return false;
	}

	private boolean verifyDependenciesInternal(Quest original, boolean firstLoop)
	{
		if (this == original && !firstLoop)
		{
			return false;
		}

		for (QuestObject object : dependencies)
		{
			if (object instanceof Quest && !((Quest) object).verifyDependenciesInternal(original, false))
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

		resetProgress(data, false);
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