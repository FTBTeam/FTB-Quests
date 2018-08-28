package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.config.ConfigEnum;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.config.ConfigInt;
import com.feed_the_beast.ftblib.lib.config.ConfigList;
import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.icon.IconAnimation;
import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbquests.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject
{
	public static final int POS_LIMIT = 25;

	public final QuestChapter chapter;
	public String description;
	public QuestType type;
	public byte x, y;
	public final List<String> text;
	public final List<String> dependencies;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	public int timesCompleted;

	public Quest(QuestChapter c, NBTTagCompound nbt)
	{
		chapter = c;
		title = nbt.getString("title");
		description = nbt.getString("description");
		icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));
		type = QuestType.NAME_MAP.get(nbt.getString("type"));
		x = (byte) MathHelper.clamp(nbt.getByte("x"), -POS_LIMIT, POS_LIMIT);
		y = (byte) MathHelper.clamp(nbt.getByte("y"), -POS_LIMIT, POS_LIMIT);
		text = new ArrayList<>();

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(list.getStringTagAt(k));
		}

		dependencies = new ArrayList<>();

		readID(nbt);

		tasks = new ArrayList<>();
		rewards = new ArrayList<>();

		list = nbt.getTagList("tasks", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			QuestTask task = QuestTaskType.createTask(this, list.getCompoundTagAt(i));

			if (task != null)
			{
				tasks.add(task);
			}
		}

		list = nbt.getTagList("rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			ItemStack stack = ItemStack.EMPTY;
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);

			if (nbt1.hasKey("type"))
			{
				switch (nbt1.getString("type"))
				{
					case "item":
						stack = ItemStackSerializer.read(nbt1.getCompoundTag("item"));
						break;
					case "xp":
						stack = new ItemStack(FTBQuestsItems.XP_VIAL);
						stack.setTagInfo("xp", new NBTTagInt(nbt1.getInteger("xp")));
						break;
					case "xp_levels":
						stack = new ItemStack(FTBQuestsItems.XP_VIAL);
						stack.setTagInfo("xp_levels", new NBTTagInt(nbt1.getInteger("xp_levels")));
						break;
					case "command":
						stack = new ItemStack(FTBQuestsItems.SCRIPT);
						stack.setTagInfo("command", new NBTTagString(nbt1.getString("command")));
						break;
				}
			}
			else
			{
				stack = ItemStackSerializer.read(nbt1);
			}

			if (!stack.isEmpty())
			{
				if (nbt1.hasKey("title"))
				{
					stack.setStackDisplayName(nbt1.getString("title"));
				}

				int uid = nbt1.getInteger("uid");

				if (uid == 0)
				{
					uid = System.identityHashCode(stack);
				}

				QuestReward reward = new QuestReward(this, uid);
				reward.stack = stack;
				reward.team = nbt1.getBoolean("team_reward");
				rewards.add(reward);
			}
		}

		list = nbt.getTagList("player_rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			ItemStack stack = ItemStackSerializer.read(nbt1);

			if (!stack.isEmpty())
			{
				QuestReward reward = new QuestReward(this, System.identityHashCode(stack));
				reward.stack = stack;
				reward.team = false;
				rewards.add(reward);
			}
		}

		list = nbt.getTagList("team_rewards", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			ItemStack stack = ItemStackSerializer.read(nbt1);

			if (!stack.isEmpty())
			{
				QuestReward reward = new QuestReward(this, System.identityHashCode(stack));
				reward.stack = stack;
				reward.team = true;
				rewards.add(reward);
			}
		}

		NBTTagList depList = nbt.getTagList("dependencies", Constants.NBT.TAG_STRING);

		for (int i = 0; i < depList.tagCount(); i++)
		{
			dependencies.add(depList.getStringTagAt(i));
		}

		timesCompleted = nbt.getInteger("times_completed");
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
			nbt.setTag("icon", ItemStackSerializer.write(icon));
		}

		if (type != QuestType.NORMAL)
		{
			nbt.setString("type", type.getName());
		}

		nbt.setByte("x", x);
		nbt.setByte("y", y);

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

		if (!dependencies.isEmpty())
		{
			NBTTagList depList = new NBTTagList();

			for (String value : dependencies)
			{
				depList.appendTag(new NBTTagString(value));
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

					if (!task.title.isEmpty())
					{
						taskNBT.setString("title", task.title);
					}

					if (!task.icon.isEmpty())
					{
						nbt.setTag("icon", ItemStackSerializer.write(task.icon));
					}

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
				if (!reward.stack.isEmpty())
				{
					NBTTagCompound nbt1 = ItemStackSerializer.write(reward.stack);
					nbt1.setInteger("uid", reward.uid);

					if (reward.team)
					{
						nbt1.setBoolean("team_reward", true);
					}

					array.appendTag(nbt1);
				}
			}

			nbt.setTag("rewards", array);
		}

		if (timesCompleted > 0)
		{
			nbt.setInteger("times_completed", timesCompleted);
		}
	}

	@Override
	public long getProgress(ITeamData data)
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
	public double getRelativeProgress(ITeamData data)
	{
		double progress = 0D;

		for (QuestTask quest : tasks)
		{
			progress += quest.getRelativeProgress(data);
		}

		return progress / (double) tasks.size();
	}

	@Override
	public boolean isComplete(ITeamData data)
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
	public void resetProgress(ITeamData data)
	{
		for (QuestTask task : tasks)
		{
			task.resetProgress(data);
		}

		data.unclaimRewards(rewards);
	}

	@Override
	public void completeInstantly(ITeamData data)
	{
		for (QuestTask task : tasks)
		{
			task.completeInstantly(data);
		}
	}

	public boolean isVisible(@Nullable ITeamData data)
	{
		if (dependencies.isEmpty())
		{
			return true;
		}
		else if (data == null)
		{
			return false;
		}

		switch (type)
		{
			case SECRET:
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
	}

	public boolean canStartTasks(ITeamData data)
	{
		if (dependencies.isEmpty())
		{
			return true;
		}

		for (String value : dependencies)
		{
			QuestObject object = chapter.file.get(value);

			if (object != null && !object.isComplete(data))
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
	}

	@Override
	public void getConfig(ConfigGroup group)
	{
		group.add("x", new ConfigInt(x, -POS_LIMIT, POS_LIMIT)
		{
			@Override
			public int getInt()
			{
				return x;
			}

			@Override
			public void setInt(int v)
			{
				x = (byte) v;
			}
		}, new ConfigInt(0));

		group.add("y", new ConfigInt(y, -POS_LIMIT, POS_LIMIT)
		{
			@Override
			public int getInt()
			{
				return y;
			}

			@Override
			public void setInt(int v)
			{
				y = (byte) v;
			}
		}, new ConfigInt(0));

		group.add("type", new ConfigEnum<QuestType>(QuestType.NAME_MAP)
		{
			@Override
			public QuestType getValue()
			{
				return type;
			}

			@Override
			public void setValue(QuestType v)
			{
				type = v;
			}
		}, new ConfigEnum<>(QuestType.NAME_MAP));

		group.add("description", new ConfigString(description)
		{
			@Override
			public String getString()
			{
				return description;
			}

			@Override
			public void setString(String v)
			{
				description = v;
			}
		}, new ConfigString(""));

		group.add("text", new ConfigList<ConfigString>(new ConfigString(""))
		{
			@Override
			public void readFromList()
			{
				text.clear();

				for (ConfigString value : list)
				{
					text.add(value.getString());
				}
			}

			@Override
			public void writeToList()
			{
				list.clear();

				for (String value : text)
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

	public void move(byte direction)
	{
		if (direction == 5 || direction == 6 || direction == 7)
		{
			if (x == -POS_LIMIT)
			{
				x = POS_LIMIT;
			}
			else
			{
				x--;
			}
		}

		if (direction == 1 || direction == 2 || direction == 3)
		{
			if (x == POS_LIMIT)
			{
				x = -POS_LIMIT;
			}
			else
			{
				x++;
			}
		}

		if (direction == 0 || direction == 1 || direction == 7)
		{
			if (y == -POS_LIMIT)
			{
				y = POS_LIMIT;
			}
			else
			{
				y--;
			}
		}

		if (direction == 3 || direction == 4 || direction == 5)
		{
			if (y == POS_LIMIT)
			{
				y = -POS_LIMIT;
			}
			else
			{
				y++;
			}
		}

		for (Quest quest : chapter.quests)
		{
			if (quest != this && quest.x == x && quest.y == y)
			{
				move(direction);
				return;
			}
		}
	}

	public boolean setDependency(QuestObject dep, boolean add)
	{
		if (dep == this)
		{
			return false;
		}

		String d = dep.getID();

		if (add)
		{
			for (String value : dependencies)
			{
				if (value.equals(d))
				{
					return false;
				}
			}

			dependencies.add(d);
			return true;
		}
		else
		{
			return dependencies.remove(d);
		}
	}

	public boolean hasDependency(QuestObject dep)
	{
		String d = dep.getID();

		for (String value : dependencies)
		{
			if (value.equals(d))
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

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();

		for (QuestTask task : tasks)
		{
			task.clearCachedData();
		}
	}
}