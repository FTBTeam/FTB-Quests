package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamLoadedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamSavedEvent;
import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
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
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskData;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

		for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					data.createTaskData(task);
				}
			}
		}

		NBTTagCompound nbt = NBTUtils.readNBT(event.getTeam().getDataFile("ftbquests"));
		data.readData(nbt == null ? new NBTTagCompound() : nbt);
	}

	@SubscribeEvent
	public static void onTeamCreated(ForgeTeamCreatedEvent event)
	{
		FTBQuestsTeamData data = get(event.getTeam());

		for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					data.createTaskData(task);
				}
			}
		}

		new MessageCreateTeamData(event.getTeam()).sendToAll();
	}

	@SubscribeEvent
	public static void onTeamDeleted(ForgeTeamDeletedEvent event)
	{
		FileUtils.delete(event.getTeam().getDataFile("ftbquests"));
		new MessageDeleteTeamData(event.getTeam().getUID()).sendToAll();
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
			t.uid = team.getUID();
			t.id = team.getID();
			t.name = team.getTitle();

			int size = 0;

			for (QuestTaskData taskData : data.taskData.values())
			{
				if (taskData.toNBT() != null)
				{
					size++;
				}
			}

			t.taskKeys = new int[size];
			t.taskValues = new NBTBase[size];
			int i = 0;

			for (QuestTaskData taskData : data.taskData.values())
			{
				NBTBase nbt = taskData.toNBT();

				if (nbt != null)
				{
					t.taskKeys[i] = taskData.task.id;
					t.taskValues[i] = nbt;
					i++;
				}
			}

			size = 0;

			for (Int2LongOpenHashMap.Entry entry : data.variables.int2LongEntrySet())
			{
				if (entry.getLongValue() > 0L)
				{
					size++;
				}
			}

			t.variableKeys = new int[size];
			t.variableValues = new long[size];
			i = 0;

			for (Int2LongOpenHashMap.Entry entry : data.variables.int2LongEntrySet())
			{
				long value = entry.getLongValue();

				if (value > 0L)
				{
					t.variableKeys[i] = entry.getIntKey();
					t.variableValues[i] = value;
					i++;
				}
			}

			teamData.add(t);
		}

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

		new MessageSyncQuests(ServerQuestFile.INSTANCE, event.getPlayer().team.getUID(), teamData, FTBQuests.canEdit(player), rewards).sendTo(player);
		event.getPlayer().getPlayer().inventoryContainer.addListener(new FTBQuestsInventoryListener(event.getPlayer().getPlayer()));
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event)
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP)
		{
			event.getEntityPlayer().inventoryContainer.addListener(new FTBQuestsInventoryListener((EntityPlayerMP) event.getEntityPlayer()));
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			ITeamData data = ServerQuestFile.INSTANCE.getData(FTBLibAPI.getTeamID(event.player.getUniqueID()));

			if (data != null)
			{
				for (QuestChapter chapter : ServerQuestFile.INSTANCE.chapters)
				{
					for (Quest quest : chapter.quests)
					{
						for (QuestTask task : quest.tasks)
						{
							if (task instanceof DimensionTask)
							{
								data.getQuestTaskData(task).submitTask((EntityPlayerMP) event.player, Collections.emptyList(), false);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onContainerOpened(PlayerContainerEvent.Open event)
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP && !(event.getContainer() instanceof ContainerPlayer))
		{
			event.getContainer().addListener(new FTBQuestsInventoryListener((EntityPlayerMP) event.getEntityPlayer()));
		}
	}

	@SubscribeEvent
	public static void onPlayerLeftTeam(ForgeTeamPlayerLeftEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam((short) 0).sendTo(event.getPlayer().getPlayer());
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinedTeam(ForgeTeamPlayerJoinedEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam(event.getTeam().getUID()).sendTo(event.getPlayer().getPlayer());
		}
	}

	private final Int2ObjectOpenHashMap<QuestTaskData> taskData;
	private final Map<UUID, IntOpenHashSet> claimedPlayerRewards;
	private final IntOpenHashSet claimedTeamRewards;
	private final Int2LongOpenHashMap variables;

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new Int2ObjectOpenHashMap<>();
		claimedPlayerRewards = new HashMap<>();
		claimedTeamRewards = new IntOpenHashSet();
		variables = new Int2LongOpenHashMap();
		variables.defaultReturnValue(0L);
	}

	@Override
	public String getID()
	{
		return FTBQuests.MOD_ID;
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
		team.markDirty();
		new MessageUpdateTaskProgress(team.getUID(), data.task.id, data.toNBT()).sendToAll();

		if (!data.isComplete && data.task.isComplete(this))
		{
			data.isComplete = true;

			if (data.task.isComplete(this))
			{
				List<EntityPlayerMP> onlinePlayers = new ArrayList<>();

				for (ForgePlayer player : team.getMembers())
				{
					if (player.isOnline())
					{
						onlinePlayers.add(player.getPlayer());
					}
				}

				data.task.onCompleted(this, onlinePlayers);
			}
		}
	}

	@Override
	public void removeTask(QuestTask task)
	{
		taskData.remove(task.id);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		QuestTaskData data = task.createData(this);
		taskData.put(task.id, data);
		data.isComplete = data.getProgress() >= data.task.getMaxProgress();
	}

	public void claimReward(EntityPlayerMP player, QuestReward reward)
	{
		if (reward.isTeamReward())
		{
			if (claimedTeamRewards.add(reward.id))
			{
				reward.claim(player);
				team.markDirty();

				for (ForgePlayer player1 : team.getMembers())
				{
					if (player1.isOnline())
					{
						new MessageClaimRewardResponse(reward.id).sendTo(player1.getPlayer());
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

			if (set.add(reward.id))
			{
				if (set.size() == 1)
				{
					claimedPlayerRewards.put(player.getUniqueID(), set);
				}

				reward.claim(player);
				team.markDirty();
				new MessageClaimRewardResponse(reward.id).sendTo(player);
			}
		}

		reward.quest.checkRepeatableQuests(FTBQuestsTeamData.get(team), player.getUniqueID());
	}

	@Override
	public boolean isRewardClaimed(UUID player, QuestReward reward)
	{
		if (reward.isTeamReward())
		{
			return claimedTeamRewards.contains(reward.id);
		}

		IntOpenHashSet rewards = claimedPlayerRewards.get(player);
		return rewards != null && rewards.contains(reward.id);
	}

	@Override
	public void unclaimRewards(Collection<QuestReward> rewards)
	{
		for (QuestReward reward : rewards)
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

		for (ForgePlayer player : team.getMembers())
		{
			if (player.isOnline())
			{
				for (QuestReward reward : rewards)
				{
					//FIXME: new MessageResetProgress(reward.uid).sendTo(player.getPlayer());
				}
			}
		}

		team.markDirty();
	}

	@Override
	public long getVariable(int variable)
	{
		return variables.get(variable);
	}

	@Override
	public void setVariable(int variable, long value)
	{
		long prevValue = getVariable(variable);

		if (value <= 0L)
		{
			variables.remove(variable);
		}
		else
		{
			QuestVariable v = ServerQuestFile.INSTANCE.getVariable(variable);

			if (v != null)
			{
				value = Math.min(value, v.maxValue);
			}

			variables.put(variable, value);
		}

		if (prevValue != value)
		{
			team.markDirty();
			new MessageUpdateVariable(team.getUID(), variable, value).sendToAll();
		}
	}

	private void writeData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = new NBTTagCompound();

		for (QuestTaskData data : taskData.values())
		{
			NBTBase nbt2 = data.toNBT();

			if (nbt2 != null)
			{
				nbt1.setTag(data.task.getCodeString(), nbt2);
			}
		}

		nbt.setTag("Tasks", nbt1);

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

			for (Int2LongOpenHashMap.Entry entry : variables.int2LongEntrySet())
			{
				QuestVariable variable = ServerQuestFile.INSTANCE.getVariable(entry.getIntKey());

				if (variable != null)
				{
					nbt1.setLong(variable.getCodeString(), entry.getLongValue());
				}
			}

			nbt.setTag("Variables", nbt1);
		}
	}

	private void readData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = nbt.getCompoundTag("Tasks");

		if (!nbt1.isEmpty())
		{
			for (QuestTaskData data : taskData.values())
			{
				data.fromNBT(nbt1.getTag(data.task.getCodeString()));
			}
		}
		else
		{
			team.markDirty();
			nbt1 = nbt.getCompoundTag("TaskData");

			for (QuestTaskData data : taskData.values())
			{
				data.fromNBT(null);
			}

			for (String c : nbt1.getKeySet())
			{
				NBTTagCompound nbt2 = nbt1.getCompoundTag(c);

				for (String q : nbt2.getKeySet())
				{
					NBTTagCompound nbt3 = nbt2.getCompoundTag(q);

					for (String t : nbt3.getKeySet())
					{
						QuestTaskData data = taskData.get(ServerQuestFile.INSTANCE.getID(c + ':' + q + ':' + t));

						if (data != null)
						{
							data.fromNBT(nbt3.getTag(t));
						}
					}
				}
			}
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
			QuestVariable variable = ServerQuestFile.INSTANCE.getVariable(ServerQuestFile.INSTANCE.getID(s));

			if (variable != null)
			{
				variables.put(variable.id, nbt1.getLong(s));
			}
		}
	}

	@Override
	public short getTeamUID()
	{
		return team.getUID();
	}

	@Override
	public String getTeamID()
	{
		return team.getID();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return team.getTitle();
	}

	@Override
	public QuestFile getFile()
	{
		return ServerQuestFile.INSTANCE;
	}

	@Override
	public QuestTaskData getQuestTaskData(QuestTask task)
	{
		QuestTaskData data = taskData.get(task.id);

		if (data == null)
		{
			return task.createData(this);
		}

		return data;
	}
}