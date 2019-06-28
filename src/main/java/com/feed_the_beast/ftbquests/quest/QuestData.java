package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public abstract class QuestData
{
	public final Int2ObjectOpenHashMap<TaskData> taskData;
	public final Map<UUID, IntOpenHashSet> claimedPlayerRewards;
	public final IntOpenHashSet claimedTeamRewards;
	public Int2ByteOpenHashMap progressCache;

	protected QuestData()
	{
		taskData = new Int2ObjectOpenHashMap<>();
		claimedPlayerRewards = new HashMap<>();
		claimedTeamRewards = new IntOpenHashSet();
		progressCache = null;
	}

	public abstract short getTeamUID();

	public abstract String getTeamID();

	public abstract ITextComponent getDisplayName();

	public abstract QuestFile getFile();

	public TaskData getTaskData(Task task)
	{
		TaskData data = taskData.get(task.id);

		if (data == null)
		{
			return task.createData(this);
		}

		return data;
	}

	@Override
	public String toString()
	{
		return getTeamID();
	}

	public void syncTask(TaskData data)
	{
		getFile().clearCachedProgress();
	}

	public void removeTask(Task task)
	{
		taskData.remove(task.id);
	}

	public void createTaskData(Task task)
	{
		taskData.put(task.id, task.createData(this));
	}

	public boolean isRewardClaimed(UUID player, Reward reward)
	{
		if (reward.isTeamReward())
		{
			return claimedTeamRewards.contains(reward.id);
		}

		IntOpenHashSet rewards = claimedPlayerRewards.get(player);
		return rewards != null && rewards.contains(reward.id);
	}

	public void unclaimRewards(Collection<Reward> rewards)
	{
		for (Reward reward : rewards)
		{
			if (reward.isTeamReward())
			{
				claimedTeamRewards.rem(reward.id);
			}
			else
			{
				Iterator<IntOpenHashSet> iterator = claimedPlayerRewards.values().iterator();

				while (iterator.hasNext())
				{
					IntOpenHashSet set = iterator.next();

					if (set != null && set.rem(reward.id))
					{
						if (set.isEmpty())
						{
							iterator.remove();
						}
					}
				}
			}
		}
	}

	public boolean setRewardClaimed(UUID player, Reward reward)
	{
		if (reward.isTeamReward())
		{
			if (claimedTeamRewards.add(reward.id))
			{
				reward.quest.checkRepeatableQuests(this, player);
				return true;
			}
		}
		else
		{
			IntOpenHashSet set = claimedPlayerRewards.get(player);

			if (set == null)
			{
				set = new IntOpenHashSet();
			}

			if (set.add(reward.id))
			{
				if (set.size() == 1)
				{
					claimedPlayerRewards.put(player, set);
				}

				reward.quest.checkRepeatableQuests(this, player);
				return true;
			}
		}

		return false;
	}

	public void checkAutoCompletion(Quest quest)
	{
	}
}