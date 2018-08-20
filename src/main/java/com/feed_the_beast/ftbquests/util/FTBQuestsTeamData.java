package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamLoadedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamSavedEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamData;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageChangedTeam;
import com.feed_the_beast.ftbquests.net.MessageCreateTeamData;
import com.feed_the_beast.ftbquests.net.MessageDeleteTeamData;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsTeamData extends TeamData implements IProgressData
{
	public static FTBQuestsTeamData get(ForgeTeam team)
	{
		return team.getData().get(FTBQuests.MOD_ID);
	}

	@SubscribeEvent
	public static void registerTeamData(ForgeTeamDataEvent event)
	{
		event.register(new FTBQuestsTeamData(event.getTeam()));
	}

	@SubscribeEvent
	public static void onTeamSaved(ForgeTeamSavedEvent event)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		FTBQuestsTeamData data = get(event.getTeam());
		data.writeData(nbt);

		File file = event.getTeam().getDataFile("ftbquests");

		if (nbt.isEmpty())
		{
			FileUtils.delete(file);
		}
		else
		{
			NBTUtils.writeNBTSafe(file, nbt);
		}
	}

	@SubscribeEvent
	public static void onTeamLoaded(ForgeTeamLoadedEvent event)
	{
		FTBQuestsTeamData data = get(event.getTeam());

		for (QuestTask task : ServerQuestFile.INSTANCE.allTasks)
		{
			data.createTaskData(task);
		}

		NBTTagCompound nbt = NBTUtils.readNBT(event.getTeam().getDataFile("ftbquests"));
		data.readData(nbt == null ? new NBTTagCompound() : nbt);
	}

	@SubscribeEvent
	public static void onTeamCreated(ForgeTeamCreatedEvent event)
	{
		FTBQuestsTeamData data = get(event.getTeam());

		for (QuestTask task : ServerQuestFile.INSTANCE.allTasks)
		{
			data.createTaskData(task);
		}

		new MessageCreateTeamData(event.getTeam().getName()).sendToAll();
	}

	@SubscribeEvent
	public static void onTeamDeleted(ForgeTeamDeletedEvent event)
	{
		FileUtils.delete(event.getTeam().getDataFile("ftbquests"));
		new MessageDeleteTeamData(event.getTeam().getName()).sendToAll();
	}

	@SubscribeEvent
	public static void onPlayerLeftTeam(ForgeTeamPlayerLeftEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam("").sendTo(event.getPlayer().getPlayer());
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinedTeam(ForgeTeamPlayerJoinedEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam(event.getTeam().getName()).sendTo(event.getPlayer().getPlayer());
		}
	}

	private final Map<QuestTask, QuestTaskData> taskData;
	private final Collection<QuestReward> claimedTeamRewards;
	private final Map<UUID, HashSet<QuestReward>> claimedPlayerRewards;

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new HashMap<>();
		claimedTeamRewards = new HashSet<>();
		claimedPlayerRewards = new HashMap<>();

		if (ServerQuestFile.INSTANCE != null)
		{
			for (QuestTask task : ServerQuestFile.INSTANCE.allTasks)
			{
				createTaskData(task);
			}
		}
	}

	@Override
	public String getName()
	{
		return FTBQuests.MOD_ID;
	}

	@Override
	public boolean claimReward(EntityPlayer player, QuestReward reward)
	{
		if (!reward.quest.isComplete(this))
		{
			return false;
		}
		else if (reward.teamReward)
		{
			if (!claimedTeamRewards.contains(reward))
			{
				claimedTeamRewards.add(reward);
				reward.reward((EntityPlayerMP) player);
				team.markDirty();
				return true;
			}
		}
		else
		{
			HashSet<QuestReward> collection = claimedPlayerRewards.get(player.getUniqueID());

			if ((collection == null || !collection.contains(reward)))
			{
				if (collection == null)
				{
					collection = new HashSet<>();
					claimedPlayerRewards.put(player.getUniqueID(), collection);
				}

				collection.add(reward);
				reward.reward((EntityPlayerMP) player);
				team.markDirty();
				return true;
			}
		}

		return false;
	}

	@Override
	public Collection<QuestReward> getClaimedRewards(EntityPlayer player)
	{
		Collection<QuestReward> rewards = claimedPlayerRewards.get(player.getUniqueID());

		if (rewards != null)
		{
			rewards = new HashSet<>(rewards);
			rewards.addAll(claimedTeamRewards);
		}
		else
		{
			rewards = claimedTeamRewards;
		}

		return rewards;
	}

	@Override
	public boolean isRewardClaimed(EntityPlayer player, QuestReward reward)
	{
		if (reward.teamReward)
		{
			return claimedTeamRewards.contains(reward);
		}

		Collection<QuestReward> rewards = claimedPlayerRewards.get(player.getUniqueID());
		return rewards != null && rewards.contains(reward);
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
		team.markDirty();
		NBTBase nbt = data.toNBT();

		for (EntityPlayerMP player : team.universe.server.getPlayerList().getPlayers())
		{
			if (team.universe.getPlayer(player).team.equalsTeam(team))
			{
				new MessageUpdateTaskProgress("", data.task.index, nbt).sendTo(player);
			}
			else
			{
				new MessageUpdateTaskProgress(team.getName(), data.task.index, nbt).sendTo(player);
			}
		}
	}

	@Override
	public void removeTask(QuestTask task)
	{
		taskData.remove(task);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		taskData.put(task, task.createData(this));
	}

	@Nullable
	private static NBTTagCompound getTagCompound(NBTTagCompound nbt, String key)
	{
		NBTBase nbt1 = nbt.getTag(key);
		return nbt1 instanceof NBTTagCompound ? (NBTTagCompound) nbt1 : null;
	}

	public NBTTagCompound serializeTaskData()
	{
		NBTTagCompound taskDataTag = new NBTTagCompound();

		for (QuestTaskData data : taskData.values())
		{
			NBTBase nbt = data.toNBT();

			if (nbt != null)
			{
				NBTTagCompound chapterTag = getTagCompound(taskDataTag, data.task.quest.chapter.id);

				if (chapterTag == null)
				{
					chapterTag = new NBTTagCompound();
					taskDataTag.setTag(data.task.quest.chapter.id, chapterTag);
				}

				NBTTagCompound questTag = getTagCompound(chapterTag, data.task.quest.id);

				if (questTag == null)
				{
					questTag = new NBTTagCompound();
					chapterTag.setTag(data.task.quest.id, questTag);
				}

				questTag.setTag(data.task.id, nbt);
			}
		}

		return taskDataTag;
	}

	public static NBTTagCompound serializeRewardData(Iterable<QuestReward> rewards)
	{
		NBTTagCompound rewardDataTag = new NBTTagCompound();

		for (QuestReward reward : rewards)
		{
			NBTTagCompound chapterTag = getTagCompound(rewardDataTag, reward.quest.chapter.id);

			if (chapterTag == null)
			{
				chapterTag = new NBTTagCompound();
				rewardDataTag.setTag(reward.quest.chapter.id, chapterTag);
			}

			NBTTagList list = chapterTag.getTagList(reward.quest.id, Constants.NBT.TAG_STRING);

			if (list.isEmpty())
			{
				chapterTag.setTag(reward.quest.id, list);
			}

			list.appendTag(new NBTTagString(reward.id));
		}

		return rewardDataTag;
	}

	private void writeData(NBTTagCompound nbt)
	{
		NBTTagCompound taskDataTag = serializeTaskData();

		if (!taskDataTag.isEmpty())
		{
			nbt.setTag("TaskData", taskDataTag);
		}

		if (!claimedTeamRewards.isEmpty())
		{
			nbt.setTag("ClaimedRewards", serializeRewardData(claimedTeamRewards));
		}

		NBTTagCompound claimedPlayerRewardsTag = new NBTTagCompound();

		for (Map.Entry<UUID, HashSet<QuestReward>> entry : claimedPlayerRewards.entrySet())
		{
			ForgePlayer player = team.universe.getPlayer(entry.getKey());

			if (player != null && !entry.getValue().isEmpty())
			{
				claimedPlayerRewardsTag.setTag(player.getName(), serializeRewardData(entry.getValue()));
			}
		}

		if (!claimedPlayerRewardsTag.isEmpty())
		{
			nbt.setTag("ClaimedPlayerRewards", claimedPlayerRewardsTag);
		}
	}

	public static void deserializeTaskData(Iterable<QuestTaskData> dataValues, NBTTagCompound taskDataTag)
	{
		for (QuestTaskData data : dataValues)
		{
			NBTBase nbt = null;
			NBTTagCompound chapterTag = getTagCompound(taskDataTag, data.task.quest.chapter.id);

			if (chapterTag != null)
			{
				NBTTagCompound questTag = getTagCompound(chapterTag, data.task.quest.id);

				if (questTag != null)
				{
					nbt = questTag.getTag(data.task.id);
				}
			}

			data.fromNBT(nbt);
		}
	}

	public static void deserializeRewardData(QuestFile file, Collection<QuestReward> rewards, NBTTagCompound taskDataTag)
	{
		for (String c : taskDataTag.getKeySet())
		{
			NBTTagCompound ctag = taskDataTag.getCompoundTag(c);

			for (String q : ctag.getKeySet())
			{
				NBTTagList list = ctag.getTagList(q, Constants.NBT.TAG_STRING);

				for (int i = 0; i < list.tagCount(); i++)
				{
					QuestReward reward = file.getReward(c + ':' + q + '#' + list.getStringTagAt(i));

					if (reward != null)
					{
						rewards.add(reward);
					}
				}
			}
		}
	}

	private void readData(NBTTagCompound nbt)
	{
		deserializeTaskData(taskData.values(), nbt.getCompoundTag("TaskData"));

		claimedTeamRewards.clear();
		deserializeRewardData(ServerQuestFile.INSTANCE, claimedTeamRewards, nbt.getCompoundTag("ClaimedRewards"));

		claimedPlayerRewards.clear();

		NBTTagCompound claimedPlayerRewardsTag = nbt.getCompoundTag("ClaimedPlayerRewards");

		for (String s : claimedPlayerRewardsTag.getKeySet())
		{
			ForgePlayer player = team.universe.getPlayer(s);

			if (player != null)
			{
				HashSet<QuestReward> rewards = new HashSet<>();
				deserializeRewardData(ServerQuestFile.INSTANCE, rewards, claimedPlayerRewardsTag.getCompoundTag(s));

				if (!rewards.isEmpty())
				{
					claimedPlayerRewards.put(player.getId(), rewards);
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
	public QuestTaskData getQuestTaskData(QuestTask task)
	{
		QuestTaskData data = taskData.get(task);

		if (data == null)
		{
			throw new IllegalArgumentException("Missing data for task " + task);
		}

		return data;
	}

	@Override
	public void unclaimReward(QuestReward reward)
	{
		if (reward.teamReward)
		{
			if (claimedTeamRewards.remove(reward))
			{
				team.markDirty();
			}
		}
		else
		{
			Iterator<HashSet<QuestReward>> itr = claimedPlayerRewards.values().iterator();

			while (itr.hasNext())
			{
				HashSet<QuestReward> collection = itr.next();

				if (collection.remove(reward))
				{
					if (collection.isEmpty())
					{
						itr.remove();
					}

					team.markDirty();
				}
			}
		}
	}
}