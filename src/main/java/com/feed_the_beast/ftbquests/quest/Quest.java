package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.rewards.QuestRewardType;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends ProgressingQuestObject
{
	public static final int POS_LIMIT = 25;

	public final QuestChapter chapter;
	public final ConfigString description;
	public final ConfigEnum<QuestType> type;
	public final ConfigInt x, y;
	public final ConfigList<ConfigString> text;
	public final ConfigList<ConfigString> dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;

	public Quest(QuestChapter c, NBTTagCompound nbt)
	{
		chapter = c;
		title = nbt.getString("title");
		description = new ConfigString(nbt.getString("description"));
		icon = QuestFile.getIcon(nbt);
		type = new ConfigEnum<>(QuestType.NAME_MAP);
		type.setValue(nbt.getString("type"));
		x = new ConfigInt(nbt.getByte("x"), -POS_LIMIT, POS_LIMIT);
		y = new ConfigInt(nbt.getByte("y"), -POS_LIMIT, POS_LIMIT);
		text = new ConfigList<>(new ConfigString(""));

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(new ConfigString(list.getStringTagAt(k)));
		}

		dependencies = new ConfigList<>(new ConfigString(""));

		readID(nbt);

		tasks = new ArrayList<>();
		rewards = new ArrayList<>();

		list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestTask task = QuestTaskType.createTask(this, list.getCompoundTagAt(k));

			if (task != null)
			{
				tasks.add(task);
			}
		}

		list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int k = 0; k < list.tagCount(); k++)
		{
			QuestReward reward = QuestRewardType.createReward(this, list.getCompoundTagAt(k));

			if (reward != null)
			{
				rewards.add(reward);
			}
		}

		NBTTagList depList = nbt.getTagList("dependencies", Constants.NBT.TAG_STRING);

		for (int i = 0; i < depList.tagCount(); i++)
		{
			dependencies.add(new ConfigString(depList.getStringTagAt(i)));
		}
	}

	@Override
	public QuestFile getQuestFile()
	{
		return chapter.file;
	}

	@Override
	public QuestObjectType getObjectType()
	{
		return QuestObjectType.QUEST;
	}

	@Override
	public String getID()
	{
		return chapter.id + ':' + id;
	}

	@Override
	public void writeData(NBTTagCompound nbt)
	{
		nbt.setString("id", id);

		if (!title.isEmpty())
		{
			nbt.setString("title", title);
		}

		if (!icon.isEmpty())
		{
			nbt.setTag("icon", icon.serializeNBT());
		}

		if (type.getValue() != QuestType.NORMAL)
		{
			nbt.setString("type", type.getValue().getName());
		}

		nbt.setByte("x", (byte) x.getInt());
		nbt.setByte("y", (byte) y.getInt());

		if (!description.isEmpty())
		{
			nbt.setString("description", description.getString());
		}

		if (!text.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (ConfigString s : text)
			{
				array.appendTag(new NBTTagString(s.getString()));
			}

			nbt.setTag("text", array);
		}

		if (!dependencies.isEmpty())
		{
			NBTTagList depList = new NBTTagList();

			for (ConfigString value : dependencies)
			{
				depList.appendTag(new NBTTagString(value.getString()));
			}

			nbt.setTag("dependencies", depList);
		}

		if (!tasks.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestTask task : tasks)
			{
				QuestTaskType type = QuestTaskType.getType(task.getClass());

				if (type != null)
				{
					NBTTagCompound taskNBT = new NBTTagCompound();
					task.writeData(taskNBT);
					taskNBT.setString("id", task.id);
					taskNBT.setString("type", type.getTypeForNBT());
					array.appendTag(taskNBT);
				}
			}

			nbt.setTag("tasks", array);
		}

		if (!rewards.isEmpty())
		{
			NBTTagList array = new NBTTagList();

			for (QuestReward reward : rewards)
			{
				QuestRewardType type = QuestRewardType.getType(reward.getClass());

				if (type != null)
				{
					NBTTagCompound rewardNBT = new NBTTagCompound();
					reward.writeData(rewardNBT);
					rewardNBT.setString("id", reward.id);
					rewardNBT.setString("type", type.getTypeForNBT());

					if (reward.teamReward)
					{
						rewardNBT.setBoolean("team_reward", true);
					}

					if (!reward.title.isEmpty())
					{
						rewardNBT.setString("title", reward.title);
					}

					array.appendTag(rewardNBT);
				}
			}

			nbt.setTag("rewards", array);
		}
	}

	@Override
	public long getProgress(IProgressData data)
	{
		long progress = 0L;

		for (QuestTask task : tasks)
		{
			progress += task.getProgress(data);
		}

		return progress;
	}

	@Override
	public long getMaxProgress()
	{
		long maxProgress = 0L;

		for (QuestTask task : tasks)
		{
			maxProgress += task.getMaxProgress();
		}

		return maxProgress;
	}

	@Override
	public double getRelativeProgress(IProgressData data)
	{
		double progress = 0D;

		for (QuestTask quest : tasks)
		{
			progress += quest.getRelativeProgress(data);
		}

		return progress / (double) tasks.size();
	}

	@Override
	public boolean isComplete(IProgressData data)
	{
		for (QuestTask task : tasks)
		{
			if (!task.isComplete(data))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void resetProgress(IProgressData data)
	{
		for (QuestTask task : tasks)
		{
			task.resetProgress(data);
		}

		for (QuestReward reward : rewards)
		{
			data.unclaimReward(reward);
		}
	}

	public boolean isVisible(IProgressData data)
	{
		if (dependencies.isEmpty())
		{
			return true;
		}

		switch (type.getValue())
		{
			case SECRET:
				for (ConfigString value : dependencies)
				{
					QuestObject object = chapter.file.get(value.getString());

					if (object instanceof ProgressingQuestObject && ((ProgressingQuestObject) object).isComplete(data))
					{
						return true;
					}
				}

				return false;
			case INVISIBLE:
				for (ConfigString value : dependencies)
				{
					QuestObject object = chapter.file.get(value.getString());

					if (object instanceof ProgressingQuestObject && !((ProgressingQuestObject) object).isComplete(data))
					{
						return false;
					}
				}

				return true;
			default:
				return true;
		}
	}

	public boolean canStartTasks(IProgressData data)
	{
		if (dependencies.isEmpty())
		{
			return true;
		}

		for (ConfigString value : dependencies)
		{
			QuestObject object = chapter.file.get(value.getString());

			if (object instanceof ProgressingQuestObject && !((ProgressingQuestObject) object).isComplete(data))
			{
				return false;
			}
		}

		return true;
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
		return new TextComponentString("");
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

		tasks.clear();

		for (QuestReward reward : rewards)
		{
			reward.deleteChildren();
			reward.invalid = true;
		}

		rewards.clear();
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("x", x, new ConfigInt(0, -POS_LIMIT, POS_LIMIT));
		group.add("y", y, new ConfigInt(0, -POS_LIMIT, POS_LIMIT));
		group.add("type", type, new ConfigEnum<>(QuestType.NAME_MAP));
		group.add("description", description, new ConfigString(""));
		group.add("text", text, new ConfigList<>(new ConfigString("")));
		group.add("dependencies", dependencies, new ConfigList<>(new ConfigString("")));
	}

	public void move(byte direction)
	{
		if (direction == 5 || direction == 6 || direction == 7)
		{
			int v = x.getInt() - 1;
			x.setInt(v <= -(POS_LIMIT + 1) ? POS_LIMIT : v);
		}

		if (direction == 1 || direction == 2 || direction == 3)
		{
			int v = x.getInt() + 1;
			x.setInt(v >= (POS_LIMIT + 1) ? -POS_LIMIT : v);
		}

		if (direction == 0 || direction == 1 || direction == 7)
		{
			int v = y.getInt() - 1;
			y.setInt(v <= -(POS_LIMIT + 1) ? POS_LIMIT : v);
		}

		if (direction == 3 || direction == 4 || direction == 5)
		{
			int v = y.getInt() + 1;
			y.setInt(v >= (POS_LIMIT + 1) ? -POS_LIMIT : v);
		}

		for (Quest quest : chapter.quests)
		{
			if (quest != this && quest.x.getInt() == x.getInt() && quest.y.getInt() == y.getInt())
			{
				move(direction);
				return;
			}
		}
	}

	public boolean setDependency(ProgressingQuestObject dep, boolean add)
	{
		if (dep == this)
		{
			return false;
		}

		String d = dep.getID();

		if (add)
		{
			for (ConfigString value : dependencies)
			{
				if (value.getString().equals(d))
				{
					return false;
				}
			}

			dependencies.add(new ConfigString(d));
			return true;
		}
		else
		{
			Iterator<ConfigString> iterator = dependencies.list.iterator();

			while (iterator.hasNext())
			{
				if (iterator.next().getString().equals(d))
				{
					iterator.remove();
					return true;
				}
			}

			return false;
		}
	}

	public boolean hasDependency(ProgressingQuestObject dep)
	{
		String d = dep.getID();

		for (ConfigString value : dependencies)
		{
			if (value.getString().equals(d))
			{
				return true;
			}
		}

		return false;
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
}