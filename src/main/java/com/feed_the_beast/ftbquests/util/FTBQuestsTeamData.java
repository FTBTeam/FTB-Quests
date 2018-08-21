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
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.net.MessageChangedTeam;
import com.feed_the_beast.ftbquests.net.MessageCreateTeamData;
import com.feed_the_beast.ftbquests.net.MessageDeleteTeamData;
import com.feed_the_beast.ftbquests.net.MessageSyncRewards;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new HashMap<>();

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
				new ObjectCompletedEvent(this, data.task).post();

				if (data.task.quest.isComplete(this))
				{
					new ObjectCompletedEvent(this, data.task.quest).post();

					if (data.task.quest.chapter.isComplete(this))
					{
						new ObjectCompletedEvent(this, data.task.quest.chapter).post();

						if (data.task.quest.chapter.file.isComplete(this))
						{
							new ObjectCompletedEvent(this, data.task.quest.chapter.file).post();
						}
					}

					ForgeTeam team = Universe.get().getTeam(getTeamID());

					if (team.isValid())
					{
						for (ForgePlayer player : team.getMembers())
						{
							FTBQuestsPlayerData data1 = FTBQuestsPlayerData.get(player);

							for (int i = 0; i < data.task.quest.rewards.size(); i++)
							{
								data1.rewards.items.add(data.task.quest.rewards.get(i).getRewardItem());
							}

							player.markDirty();

							if (player.isOnline())
							{
								new MessageSyncRewards(data1.rewards.items).sendTo(player.getPlayer());
							}
						}
					}
				}
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
}