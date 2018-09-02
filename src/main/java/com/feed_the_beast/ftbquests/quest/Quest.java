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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public final class Quest extends QuestObject
{
	public static final int POS_LIMIT = 25;

	public final QuestChapter chapter;
	public String description;
	public EnumQuestVisibilityType visibilityType;
	public byte x, y;
	public final List<String> text;
	public final List<QuestTask> tasks;
	public final List<QuestReward> rewards;
	public int timesCompleted;

	private String cachedID = "";
	private Collection<QuestObject> cachedDependencies;

	public Quest(QuestChapter c, NBTTagCompound nbt)
	{
		chapter = c;
		readID(nbt);
		title = nbt.getString("title");
		description = nbt.getString("description");
		icon = ItemStackSerializer.read(nbt.getCompoundTag("icon"));
		completionCommand = nbt.getString("completion_command");
		visibilityType = EnumQuestVisibilityType.NAME_MAP.get(nbt.getString("visibility"));
		x = (byte) MathHelper.clamp(nbt.getByte("x"), -POS_LIMIT, POS_LIMIT);
		y = (byte) MathHelper.clamp(nbt.getByte("y"), -POS_LIMIT, POS_LIMIT);
		text = new ArrayList<>();

		NBTTagList list = nbt.getTagList("text", Constants.NBT.TAG_STRING);

		for (int k = 0; k < list.tagCount(); k++)
		{
			text.add(list.getStringTagAt(k));
		}

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
		if (cachedID.isEmpty())
		{
			cachedID = chapter.id + ':' + id;
		}

		return cachedID;
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

		if (!completionCommand.isEmpty())
		{
			nbt.setString("completion_command", completionCommand);
		}

		if (visibilityType != EnumQuestVisibilityType.NORMAL)
		{
			nbt.setString("type", visibilityType.getName());
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
						taskNBT.setTag("icon", ItemStackSerializer.write(task.icon));
					}

					if (!task.completionCommand.isEmpty())
					{
						taskNBT.setString("completion_command", task.completionCommand);
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
	public int getRelativeProgress(ITeamData data)
	{
		int progress = 0;

		for (QuestTask quest : tasks)
		{
			progress += quest.getRelativeProgress(data);
		}

		return fixRelativeProgress(progress, tasks.size());
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
	public void onCompleted(ITeamData data)
	{
		timesCompleted++;
		super.onCompleted(data);

		if (chapter.isComplete(data))
		{
			chapter.onCompleted(data);
		}
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

	public boolean canStartTasks(ITeamData data)
	{
		return true;
	}

	@Override
	public Icon getAltIcon()
	{
		List<Icon> list = new ArrayList<>();

		for (QuestTask task : tasks)
		{
			if (task.getDependency() == null)
			{
				list.add(task.getIcon());
			}
		}

		return IconAnimation.fromList(list, false);
	}

	@Override
	public ITextComponent getAltDisplayName()
	{
		for (QuestTask task : tasks)
		{
			QuestObject dep = task.getDependency();

			if (dep != null)
			{
				return dep.getDisplayName();
			}
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

		tasks.clear();
	}

	@Override
	public void onCreated()
	{
		chapter.quests.add(this);
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
	}

	public EnumVisibility getVisibility(@Nullable ITeamData data)
	{
		EnumVisibility v = EnumVisibility.VISIBLE;

		/*
		for (QuestObject object : getDependencies())
		{
			v = v.weakest(object.getVisibility(data));

			if (v.isInvisible())
			{
				return EnumVisibility.INVISIBLE;
			}
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
		cachedID = "";
		cachedDependencies = null;

		for (QuestTask task : tasks)
		{
			task.clearCachedData();
		}
	}
}