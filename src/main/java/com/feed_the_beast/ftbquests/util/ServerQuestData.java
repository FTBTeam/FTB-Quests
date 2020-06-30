package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamChangedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamLoadedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamSavedEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.NBTDataStorage;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageChangedTeam;
import com.feed_the_beast.ftbquests.net.MessageClaimRewardResponse;
import com.feed_the_beast.ftbquests.net.MessageCreateTeamData;
import com.feed_the_beast.ftbquests.net.MessageDeleteTeamData;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.task.DimensionTask;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBQuests.MOD_ID)
public class ServerQuestData extends QuestData implements NBTDataStorage.Data
{
	public static ServerQuestData get(ForgeTeam team)
	{
		return team.getData().get(FTBQuests.MOD_ID);
	}

	@SubscribeEvent
	public static void registerTeamData(ForgeTeamDataEvent event)
	{
		event.register(new ServerQuestData(event.getTeam()));
	}

	@SubscribeEvent
	public static void onTeamSaved(ForgeTeamSavedEvent event)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		ServerQuestData data = get(event.getTeam());
		data.writeData(nbt);

		File file = event.getTeam().getDataFile("ftbquests");

		if (nbt.isEmpty())
		{
			FileUtils.deleteSafe(file);
		}
		else
		{
			NBTUtils.writeNBTSafe(file, nbt);
		}
	}

	@SubscribeEvent
	public static void onTeamLoaded(ForgeTeamLoadedEvent event)
	{
		ServerQuestData data = get(event.getTeam());

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (Task task : quest.tasks)
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
		ServerQuestData data = get(event.getTeam());

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (Task task : quest.tasks)
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
		FileUtils.deleteSafe(event.getTeam().getDataFile("ftbquests"));
		new MessageDeleteTeamData(event.getTeam().getUID()).sendToAll();
	}

	@SubscribeEvent
	public static void onPlayerLeftTeam(ForgeTeamPlayerLeftEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam(event.getPlayer().getId(), (short) 0).sendTo(event.getPlayer().getPlayer());
		}
	}

	@SubscribeEvent
	public static void onPlayerJoinedTeam(ForgeTeamPlayerJoinedEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			new MessageChangedTeam(event.getPlayer().getId(), event.getTeam().getUID()).sendTo(event.getPlayer().getPlayer());
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(ForgePlayerLoggedInEvent event)
	{
		ServerQuestData teamData = ServerQuestData.get(event.getTeam());
		EntityPlayerMP player = event.getPlayer().getPlayer();

		MessageSyncQuests m = new MessageSyncQuests();
		m.file = ServerQuestFile.INSTANCE;
		m.team = teamData.getTeamUID();
		m.teamData = new ArrayList<>();

		for (ForgeTeam team : event.getUniverse().getTeams())
		{
			ServerQuestData data = get(team);
			MessageSyncQuests.TeamInst t = new MessageSyncQuests.TeamInst();
			t.uid = team.getUID();
			t.id = team.getId();
			t.name = team.getTitle();

			int size = 0;

			for (TaskData taskData : data.taskData.values())
			{
				if (taskData.isStarted())
				{
					size++;
				}
			}

			t.taskKeys = new int[size];
			t.taskValues = new long[size];
			int i = 0;

			for (TaskData taskData : data.taskData.values())
			{
				if (taskData.isStarted())
				{
					t.taskKeys[i] = taskData.task.id;
					t.taskValues[i] = taskData.progress;
					i++;
				}
			}

			size = 0;

			for (Map.Entry<UUID, IntOpenHashSet> entry : data.claimedPlayerRewards.entrySet())
			{
				if (!entry.getValue().isEmpty())
				{
					size++;
				}
			}

			t.playerRewardUUIDs = new UUID[size];
			t.playerRewardIDs = new int[size][];
			i = 0;

			for (Map.Entry<UUID, IntOpenHashSet> entry : data.claimedPlayerRewards.entrySet())
			{
				if (!entry.getValue().isEmpty())
				{
					t.playerRewardUUIDs[i] = entry.getKey();
					t.playerRewardIDs[i] = entry.getValue().toIntArray();
					i++;
				}
			}

			t.teamRewards = data.claimedTeamRewards.toIntArray();
			m.teamData.add(t);
		}

		m.editingMode = FTBQuests.canEdit(player);
		m.playerIDs = new UUID[event.getUniverse().getPlayers().size()];
		m.playerTeams = new short[m.playerIDs.length];

		int i = 0;

		for (ForgePlayer p : event.getUniverse().getPlayers())
		{
			m.playerIDs[i] = p.getId();
			m.playerTeams[i] = p.team.getUID();
			i++;
		}

		m.favorites = NBTUtils.getPersistedData(player, false).getIntArray("ftbquests_pinned");

		m.sendTo(player);
		event.getPlayer().getPlayer().inventoryContainer.addListener(new FTBQuestsInventoryListener(event.getPlayer().getPlayer()));

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				teamData.checkAutoCompletion(quest);
			}
		}
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
			QuestData data = ServerQuestFile.INSTANCE.getData(event.player);

			if (data != null)
			{
				for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
				{
					for (Quest quest : chapter.quests)
					{
						for (Task task : quest.tasks)
						{
							if (task instanceof DimensionTask)
							{
								data.getTaskData(task).submitTask((EntityPlayerMP) event.player);
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
	public static void onTeamChanged(ForgeTeamChangedEvent event)
	{
		ServerQuestData oldData = get(event.getOldTeam());
		ServerQuestData newData = get(event.getTeam());
		newData.progressCache = null;
		newData.areDependenciesCompleteCache = null;

		for (TaskData oldTaskData : oldData.taskData.values())
		{
			TaskData newTaskData = newData.getTaskData(oldTaskData.task);
			newTaskData.setProgress(Math.max(oldTaskData.progress, newTaskData.progress));
		}
	}

	public final ForgeTeam team;

	private ServerQuestData(ForgeTeam t)
	{
		team = t;
	}

	@Override
	public String getId()
	{
		return FTBQuests.MOD_ID;
	}

	@Override
	public short getTeamUID()
	{
		return team.getUID();
	}

	@Override
	public String getTeamID()
	{
		return team.getId();
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
	public List<EntityPlayerMP> getOnlineMembers()
	{
		return team.getOnlineMembers();
	}

	@Override
	public void markDirty()
	{
		team.markDirty();
	}

	@Override
	public boolean setRewardClaimed(UUID player, Reward reward)
	{
		if (super.setRewardClaimed(player, reward))
		{
			markDirty();
			new MessageClaimRewardResponse(getTeamUID(), player, reward.id).sendToAll();
			return true;
		}

		return false;
	}

	public void claimReward(EntityPlayerMP player, Reward reward, boolean notify)
	{
		if (setRewardClaimed(player.getUniqueID(), reward))
		{
			reward.claim(player, notify);
		}
	}

	private void writeData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = new NBTTagCompound();

		for (TaskData data : taskData.values())
		{
			if (data.isStarted())
			{
				String key = QuestObjectBase.getCodeString(data.task);

				if (data.progress <= Byte.MAX_VALUE)
				{
					nbt1.setByte(key, (byte) data.progress);
				}
				else if (data.progress <= Short.MAX_VALUE)
				{
					nbt1.setShort(key, (short) data.progress);
				}
				else if (data.progress <= Integer.MAX_VALUE)
				{
					nbt1.setInteger(key, (int) data.progress);
				}
				else
				{
					nbt1.setLong(key, data.progress);
				}
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
	}

	private void readData(NBTTagCompound nbt)
	{
		NBTTagCompound nbt1 = nbt.getCompoundTag("Tasks");

		for (String s : nbt1.getKeySet())
		{
			Task task = ServerQuestFile.INSTANCE.getTask(ServerQuestFile.INSTANCE.getID(s));

			if (task != null)
			{
				taskData.get(task.id).readProgress(nbt1.getLong(s));
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
	}

	@Override
	public void checkAutoCompletion(Quest quest)
	{
		if (quest.rewards.isEmpty() || !quest.isComplete(this))
		{
			return;
		}

		List<EntityPlayerMP> online = null;

		for (Reward reward : quest.rewards)
		{
			RewardAutoClaim auto = reward.getAutoClaimType();

			if (auto != RewardAutoClaim.DISABLED)
			{
				if (online == null)
				{
					online = new ArrayList<>();

					for (ForgePlayer player : team.getMembers())
					{
						if (player.isOnline())
						{
							online.add(player.getPlayer());
						}
					}

					if (online.isEmpty())
					{
						return;
					}
				}

				for (EntityPlayerMP player : online)
				{
					claimReward(player, reward, auto == RewardAutoClaim.ENABLED);
				}
			}
		}
	}
}