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
import com.feed_the_beast.ftbquests.net.MessageUpdateVariable;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
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
		Collection<MessageSyncQuests.TeamInst> teamData = new ArrayList<>();

		for (ForgeTeam team : event.getUniverse().getTeams())
		{
			FTBQuestsTeamData data = get(team);
			MessageSyncQuests.TeamInst t = new MessageSyncQuests.TeamInst();
			t.name = team.getName();

			int size = 0;

			for (QuestTaskData taskData : data.taskData.values())
			{
				if (taskData.toNBT() != null)
				{
					size++;
				}
			}

			t.taskKeys = new short[size];
			t.taskValues = new NBTBase[size];
			int i = 0;

			for (QuestTaskData taskData : data.taskData.values())
			{
				NBTBase nbt = taskData.toNBT();

				if (nbt != null)
				{
					t.taskKeys[i] = (short) taskData.task.uid;
					t.taskValues[i] = nbt;
					i++;
				}
			}

			size = 0;

			for (Object2LongOpenHashMap.Entry<QuestVariable> entry : data.variables.object2LongEntrySet())
			{
				if (entry.getLongValue() > 0L)
				{
					size++;
				}
			}

			t.variableKeys = new short[size];
			t.variableValues = new long[size];
			i = 0;

			for (Object2LongOpenHashMap.Entry<QuestVariable> entry : data.variables.object2LongEntrySet())
			{
				long value = entry.getLongValue();

				if (value > 0L)
				{
					t.variableKeys[i] = entry.getKey().index;
					t.variableValues[i] = value;
					i++;
				}
			}

			teamData.add(t);
		}

		NBTTagCompound nbt = new NBTTagCompound();
		ServerQuestFile.INSTANCE.writeData(nbt);

		FTBQuestsTeamData team = FTBQuestsTeamData.get(event.getTeam());
		IntOpenHashSet rewards = team.claimedPlayerRewards.get(event.getPlayer().getId());

		if (rewards == null)
		{
			rewards = team.claimedTeamRewards;
		}
		else
		{
			rewards = new IntOpenHashSet(rewards);
			rewards.addAll(team.claimedTeamRewards);
		}

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
	public final Object2LongOpenHashMap<QuestVariable> variables;

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new HashMap<>();
		claimedPlayerRewards = new HashMap<>();
		claimedTeamRewards = new IntOpenHashSet();
		variables = new Object2LongOpenHashMap<>();
		variables.defaultReturnValue(0L);
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
				new MessageUpdateTaskProgress("", data.task.uid, nbt).sendTo(player);
			}
			else
			{
				new MessageUpdateTaskProgress(team.getName(), data.task.uid, nbt).sendTo(player);
			}
		}

		if (!data.isComplete && data.task.isComplete(this))
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

	public void claimReward(EntityPlayerMP player, QuestReward reward)
	{
		if (reward.team)
		{
			if (claimedTeamRewards.add(reward.uid))
			{
				reward.claim(player);
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

				reward.claim(player);
				team.markDirty();
				new MessageClaimRewardResponse(reward.uid).sendTo(player);
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

	@Override
	public long getVariable(QuestVariable variable)
	{
		return variables.getLong(variable);
	}

	@Override
	public void setVariable(QuestVariable variable, long value)
	{
		long prevValue = getVariable(variable);

		if (value <= 0L)
		{
			variables.removeLong(variable);
		}
		else
		{
			value = Math.min(value, variable.maxValue);
			variables.put(variable, value);
		}

		if (prevValue != value)
		{
			team.markDirty();

			for (EntityPlayerMP player : team.universe.server.getPlayerList().getPlayers())
			{
				if (team.universe.getPlayer(player).team.equalsTeam(team))
				{
					new MessageUpdateVariable("", variable.index, value).sendTo(player);
				}
				else
				{
					new MessageUpdateVariable(team.getName(), variable.index, value).sendTo(player);
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

	private void writeData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = new NBTTagCompound();

		for (QuestTaskData data : taskData.values())
		{
			NBTBase nbt2 = data.toNBT();

			if (nbt2 != null)
			{
				NBTTagCompound chapterTag = getTagCompound(nbt1, data.task.quest.chapter.id);

				if (chapterTag == null)
				{
					chapterTag = new NBTTagCompound();
					nbt1.setTag(data.task.quest.chapter.id, chapterTag);
				}

				NBTTagCompound questTag = getTagCompound(chapterTag, data.task.quest.id);

				if (questTag == null)
				{
					questTag = new NBTTagCompound();
					chapterTag.setTag(data.task.quest.id, questTag);
				}

				questTag.setTag(data.task.id, nbt2);
			}
		}

		if (!nbt1.isEmpty())
		{
			nbt.setTag("TaskData", nbt1);
		}

		if (!claimedTeamRewards.isEmpty())
		{
			nbt.setIntArray("ClaimedTeamRewards", claimedTeamRewards.toIntArray());
		}

		if (!claimedPlayerRewards.isEmpty())
		{
			nbt1 = new NBTTagCompound();

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

		if (!variables.isEmpty())
		{
			nbt1 = new NBTTagCompound();

			for (Object2LongOpenHashMap.Entry<QuestVariable> entry : variables.object2LongEntrySet())
			{
				nbt1.setLong(entry.getKey().id, entry.getLongValue());
			}

			nbt.setTag("Variables", nbt1);
		}
	}

	private void readData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = nbt.getCompoundTag("TaskData");

		for (QuestTaskData data : taskData.values())
		{
			NBTBase nbt2 = null;
			NBTTagCompound chapterTag = getTagCompound(nbt1, data.task.quest.chapter.id);

			if (chapterTag != null)
			{
				NBTTagCompound questTag = getTagCompound(chapterTag, data.task.quest.id);

				if (questTag != null)
				{
					nbt2 = questTag.getTag(data.task.id);
				}
			}

			data.fromNBT(nbt2);
		}

		claimedTeamRewards.clear();

		for (int r : nbt.getIntArray("ClaimedTeamRewards"))
		{
			claimedTeamRewards.add(r);
		}

		claimedPlayerRewards.clear();

		nbt1 = nbt.getCompoundTag("ClaimedPlayerRewards");

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

		variables.clear();

		nbt1 = nbt.getCompoundTag("Variables");

		for (String s : nbt1.getKeySet())
		{
			QuestVariable variable = ServerQuestFile.INSTANCE.getVariable('#' + s);

			if (variable != null)
			{
				variables.put(variable, nbt1.getLong(s));
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