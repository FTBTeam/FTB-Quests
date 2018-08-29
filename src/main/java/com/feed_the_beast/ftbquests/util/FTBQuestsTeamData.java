package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
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
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageChangedTeam;
import com.feed_the_beast.ftbquests.net.MessageClaimRewardResponse;
import com.feed_the_beast.ftbquests.net.MessageCreateTeamData;
import com.feed_the_beast.ftbquests.net.MessageDeleteTeamData;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class FTBQuestsTeamData extends TeamData implements ITeamData
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
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		EntityPlayerMP player = event.getPlayer().getPlayer();
		NBTTagCompound teamData = new NBTTagCompound();

		for (ForgeTeam team : event.getUniverse().getTeams())
		{
			teamData.setTag(team.getName(), FTBQuestsTeamData.get(team).serializeTaskData());
		}

		NBTTagCompound nbt = new NBTTagCompound();
		ServerQuestFile.INSTANCE.writeData(nbt);

		IntCollection rewards = FTBQuestsTeamData.get(event.getTeam()).getClaimedRewards(event.getPlayer().getId());
		new MessageSyncQuests(nbt, event.getPlayer().team.getName(), teamData, FTBQuests.canEdit(player), rewards).sendTo(player);
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
	public final Map<UUID, IntOpenHashSet> claimedPlayerRewards;
	public final IntOpenHashSet claimedTeamRewards;

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new HashMap<>();
		claimedPlayerRewards = new HashMap<>();
		claimedTeamRewards = new IntOpenHashSet();
	}

	@Override
	public String getName()
	{
		return FTBQuests.MOD_ID;
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

		boolean isComplete = data.task.isComplete(this);

		if (isComplete && !data.isComplete)
		{
			data.isComplete = true;

			if (data.task.isComplete(this))
			{
				data.task.onCompleted(this);
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
		QuestTaskData data = task.createData(this);
		taskData.put(task, data);
		data.isComplete = data.getProgress() >= data.task.getMaxProgress();
	}

	@Override
	public IntCollection getClaimedRewards(UUID player)
	{
		IntOpenHashSet p = claimedPlayerRewards.get(player);

		if (p == null)
		{
			return claimedTeamRewards;
		}

		IntOpenHashSet s = new IntOpenHashSet(claimedTeamRewards);
		s.addAll(p);
		return s;
	}

	@Override
	public boolean isRewardClaimed(UUID player, QuestReward reward)
	{
		if (reward.team)
		{
			return claimedTeamRewards.contains(reward.uid);
		}
		else
		{
			IntOpenHashSet set = claimedPlayerRewards.get(player);

			if (set != null)
			{
				return set.contains(reward.uid);
			}
		}

		return false;
	}

	@Override
	public void claimReward(EntityPlayer player, QuestReward reward)
	{
		if (reward.team)
		{
			if (claimedTeamRewards.add(reward.uid))
			{
				ItemHandlerHelper.giveItemToPlayer(player, reward.stack.copy());
				team.markDirty();

				for (ForgePlayer player1 : team.getMembers())
				{
					if (player1.isOnline())
					{
						new MessageClaimRewardResponse(reward.uid).sendTo(player1.getPlayer());
					}
				}
			}
		}
		else
		{
			IntOpenHashSet set = claimedPlayerRewards.get(player.getUniqueID());

			if (set == null)
			{
				set = new IntOpenHashSet();
			}

			if (set.add(reward.uid))
			{
				if (set.size() == 1)
				{
					claimedPlayerRewards.put(player.getUniqueID(), set);
				}

				ItemHandlerHelper.giveItemToPlayer(player, reward.stack.copy());
				team.markDirty();
				new MessageClaimRewardResponse(reward.uid).sendTo((EntityPlayerMP) player);
			}
		}
	}

	@Override
	public void unclaimRewards(Collection<QuestReward> rewards)
	{
		for (QuestReward reward : rewards)
		{
			if (reward.team)
			{
				claimedTeamRewards.rem(reward.uid);
			}
			else
			{
				Iterator<IntOpenHashSet> iterator = claimedPlayerRewards.values().iterator();

				while (iterator.hasNext())
				{
					IntOpenHashSet set = iterator.next();

					if (set != null && set.rem(reward.uid))
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

	private void writeData(NBTTagCompound nbt)
	{
		NBTTagCompound taskDataTag = serializeTaskData();

		if (!taskDataTag.isEmpty())
		{
			nbt.setTag("TaskData", taskDataTag);
		}

		if (!claimedTeamRewards.isEmpty())
		{
			nbt.setIntArray("ClaimedTeamRewards", claimedTeamRewards.toIntArray());
		}

		if (!claimedPlayerRewards.isEmpty())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();

			for (Map.Entry<UUID, IntOpenHashSet> entry : claimedPlayerRewards.entrySet())
			{
				if (!entry.getValue().isEmpty())
				{
					nbt1.setIntArray(StringUtils.fromUUID(entry.getKey()), entry.getValue().toIntArray());
				}
			}

			if (!nbt1.isEmpty())
			{
				nbt.setTag("ClaimedPlayerRewards", nbt1);
			}
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

	private void readData(NBTTagCompound nbt)
	{
		deserializeTaskData(taskData.values(), nbt.getCompoundTag("TaskData"));
		claimedTeamRewards.clear();

		for (int r : nbt.getIntArray("ClaimedTeamRewards"))
		{
			claimedTeamRewards.add(r);
		}

		claimedPlayerRewards.clear();

		NBTTagCompound nbt1 = nbt.getCompoundTag("ClaimedPlayerRewards");

		for (String s : nbt1.getKeySet())
		{
			UUID id = StringUtils.fromString(s);

			if (id != null)
			{
				int[] ar = nbt1.getIntArray(s);

				if (ar.length > 0)
				{
					IntOpenHashSet set = new IntOpenHashSet(ar.length);

					for (int r : ar)
					{
						set.add(r);
					}

					claimedPlayerRewards.put(id, set);
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
			return task.createData(this);
		}

		return data;
	}
}