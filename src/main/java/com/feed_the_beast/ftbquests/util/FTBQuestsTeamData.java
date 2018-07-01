package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBQuestsTeamData implements INBTSerializable<NBTTagCompound>, IProgressData
{
	public static FTBQuestsTeamData get(ForgeTeam team)
	{
		return team.getData(FTBQuests.MOD_ID);
	}

	public final ForgeTeam team;
	public final Int2ObjectMap<QuestTaskData> taskData;
	private final IntCollection claimedRewards;
	private final Map<UUID, IntCollection> claimedPlayerRewards;

	public FTBQuestsTeamData(ForgeTeam t)
	{
		team = t;
		taskData = new Int2ObjectOpenHashMap<>();
		claimedRewards = new IntOpenHashSet();
		claimedPlayerRewards = new HashMap<>();
	}

	@Override
	public boolean claimReward(EntityPlayer player, QuestReward reward)
	{
		if (!reward.parent.isComplete(this))
		{
			return false;
		}
		else if (reward.teamReward)
		{
			if (!claimedRewards.contains(reward.id))
			{
				claimedRewards.add(reward.id);
				reward.reward((EntityPlayerMP) player);
				team.markDirty();
				return true;
			}
		}
		else
		{
			IntCollection collection = claimedPlayerRewards.get(player.getUniqueID());

			if ((collection == null || !collection.contains(reward.id)))
			{
				if (collection == null)
				{
					collection = new IntOpenHashSet();
					claimedPlayerRewards.put(player.getUniqueID(), collection);
				}

				collection.add(reward.id);
				reward.reward((EntityPlayerMP) player);
				team.markDirty();
				return true;
			}
		}

		return false;
	}

	@Override
	public IntCollection getClaimedRewards(EntityPlayer player)
	{
		IntCollection rewards = claimedPlayerRewards.get(player.getUniqueID());

		if (rewards != null)
		{
			rewards = new IntOpenHashSet(rewards);
			rewards.addAll(claimedRewards);
		}
		else
		{
			rewards = claimedRewards;
		}

		return rewards;
	}

	@Override
	public boolean isRewardClaimed(EntityPlayer player, QuestReward reward)
	{
		if (reward.teamReward)
		{
			return claimedRewards.contains(reward.id);
		}

		IntCollection rewards = claimedPlayerRewards.get(player.getUniqueID());
		return rewards != null && rewards.contains(reward.id);
	}

	@Override
	public void syncTaskProgress(QuestTask task, int progress)
	{
		team.markDirty();

		for (EntityPlayerMP player : team.universe.server.getPlayerList().getPlayers())
		{
			if (team.universe.getPlayer(player).team.equalsTeam(team))
			{
				new MessageUpdateTaskProgress(task.id, progress).sendTo(player);
			}
		}
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();

		NBTTagCompound taskDataTag = new NBTTagCompound();

		for (QuestTaskData data : taskData.values())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();

			if (!nbt1.hasNoTags())
			{
				taskDataTag.setTag(Integer.toString(data.task.id), nbt1);
			}
		}

		nbt.setTag("TaskData", taskDataTag);
		nbt.setIntArray("ClaimedRewards", claimedRewards.toIntArray());

		NBTTagCompound claimedPlayerRewardsTag = new NBTTagCompound();

		for (Map.Entry<UUID, IntCollection> entry : claimedPlayerRewards.entrySet())
		{
			ForgePlayer player = team.universe.getPlayer(entry.getKey());

			if (player != null)
			{
				int[] ai = entry.getValue().toIntArray();

				if (ai.length > 0)
				{
					claimedPlayerRewardsTag.setIntArray(player.getName(), ai);
				}
			}
		}

		nbt.setTag("ClaimedPlayerRewards", claimedPlayerRewardsTag);

		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		NBTTagCompound taskDataTag = nbt.getCompoundTag("TaskData");

		for (QuestTaskData data : taskData.values())
		{
			data.readFromNBT(taskDataTag.getCompoundTag(Integer.toString(data.task.id)));
		}

		claimedRewards.clear();

		for (int id : nbt.getIntArray("ClaimedRewards"))
		{
			claimedRewards.add(id);
		}

		claimedPlayerRewards.clear();

		NBTTagCompound claimedPlayerRewardsTag = nbt.getCompoundTag("ClaimedPlayerRewards");

		for (String s : claimedPlayerRewardsTag.getKeySet())
		{
			ForgePlayer player = team.universe.getPlayer(s);

			if (player != null)
			{
				int[] ai = claimedPlayerRewardsTag.getIntArray(s);

				if (ai.length > 0)
				{
					claimedPlayerRewards.put(player.getId(), new IntOpenHashSet(ai));
				}
			}
		}
	}

	@Override
	public String getTeamID()
	{
		return team.getName();
	}

	@Override
	public QuestTaskData getQuestTaskData(int task)
	{
		QuestTaskData data = taskData.get(task);

		if (data == null)
		{
			data = ServerQuestList.INSTANCE.getTask(task).createData(this);
			taskData.put(task, data);
		}

		return data;
	}

	public void reset()
	{
		claimedRewards.clear();
		claimedPlayerRewards.clear();

		for (QuestTaskData data : taskData.values())
		{
			data.setProgress(0, false);
		}
	}
}