package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamLoadedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamSavedEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamData;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.HashMap;
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
	public static void saveData(ForgeTeamSavedEvent event)
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
	public static void loadData(ForgeTeamLoadedEvent event)
	{
		FTBQuestsTeamData data = get(event.getTeam());
		NBTTagCompound nbt = NBTUtils.readNBT(event.getTeam().getDataFile("ftbquests"));
		data.readData(nbt == null ? new NBTTagCompound() : nbt);
	}

	@SubscribeEvent
	public static void onTeamCreated(ForgeTeamCreatedEvent event)
	{
		FTBQuestsTeamData data = get(event.getTeam());

		for (QuestChapter chapter : ServerQuestList.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				for (QuestTask task : quest.tasks)
				{
					data.createTaskData(task);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLeftTeam(ForgeTeamPlayerLeftEvent event)
	{
		if (event.getPlayer().isOnline())
		{
			ServerQuestList.INSTANCE.sync(event.getPlayer().getPlayer());
		}
	}

	private final Int2ObjectMap<QuestTaskData> taskData;
	private final ShortCollection claimedRewards;
	private final Map<UUID, ShortCollection> claimedPlayerRewards;

	private FTBQuestsTeamData(ForgeTeam team)
	{
		super(team);
		taskData = new Int2ObjectOpenHashMap<>();
		claimedRewards = new ShortOpenHashSet();
		claimedPlayerRewards = new HashMap<>();

		if (ServerQuestList.INSTANCE != null)
		{
			for (QuestChapter chapter : ServerQuestList.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						taskData.put(task.id, task.createData(this));
					}
				}
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
		else if (reward.teamReward.getBoolean())
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
			ShortCollection collection = claimedPlayerRewards.get(player.getUniqueID());

			if ((collection == null || !collection.contains(reward.id)))
			{
				if (collection == null)
				{
					collection = new ShortOpenHashSet();
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
	public ShortCollection getClaimedRewards(EntityPlayer player)
	{
		ShortCollection rewards = claimedPlayerRewards.get(player.getUniqueID());

		if (rewards != null)
		{
			rewards = new ShortOpenHashSet(rewards);
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
		if (reward.teamReward.getBoolean())
		{
			return claimedRewards.contains(reward.id);
		}

		ShortCollection rewards = claimedPlayerRewards.get(player.getUniqueID());
		return rewards != null && rewards.contains(reward.id);
	}

	@Override
	public void syncTask(QuestTaskData data)
	{
		team.markDirty();
		NBTTagCompound nbt = new NBTTagCompound();
		data.writeToNBT(nbt);

		for (EntityPlayerMP player : team.universe.server.getPlayerList().getPlayers())
		{
			if (team.universe.getPlayer(player).team.equalsTeam(team))
			{
				new MessageUpdateTaskProgress(data.task.id, nbt).sendTo(player);
			}
		}
	}

	@Override
	public void removeTask(short task)
	{
		taskData.remove(task);
	}

	@Override
	public void createTaskData(QuestTask task)
	{
		taskData.put(task.id, task.createData(this));
	}

	public NBTTagCompound serializeTaskData()
	{
		NBTTagCompound taskDataTag = new NBTTagCompound();

		for (QuestTaskData data : taskData.values())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			data.writeToNBT(nbt1);

			if (nbt1.getSize() == 1 && nbt1.hasKey("Progress", Constants.NBT.TAG_ANY_NUMERIC))
			{
				taskDataTag.setInteger(Integer.toString(data.task.id), nbt1.getInteger("Progress"));
			}
			else if (!nbt1.isEmpty())
			{
				taskDataTag.setTag(Integer.toString(data.task.id), nbt1);
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

		if (!claimedRewards.isEmpty())
		{
			int[] ai = new int[claimedRewards.size()];
			int i = 0;

			for (short s : claimedRewards)
			{
				ai[i] = s & 0xFFFF;
				i++;
			}

			nbt.setIntArray("ClaimedRewards", ai);
		}

		NBTTagCompound claimedPlayerRewardsTag = new NBTTagCompound();

		for (Map.Entry<UUID, ShortCollection> entry : claimedPlayerRewards.entrySet())
		{
			ForgePlayer player = team.universe.getPlayer(entry.getKey());

			if (player != null && !entry.getValue().isEmpty())
			{
				int[] ai = new int[entry.getValue().size()];
				int i = 0;

				for (short s : entry.getValue())
				{
					ai[i] = s & 0xFFFF;
					i++;
				}

				claimedPlayerRewardsTag.setIntArray(player.getName(), ai);
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
			String key = Integer.toString(data.task.id);

			if (taskDataTag.hasKey(key, Constants.NBT.TAG_COMPOUND))
			{
				data.readFromNBT(taskDataTag.getCompoundTag(key));
			}
			else
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("Progress", taskDataTag.getInteger(key));
				data.readFromNBT(nbt);
			}
		}
	}

	private void readData(NBTTagCompound nbt)
	{
		deserializeTaskData(taskData.values(), nbt.getCompoundTag("TaskData"));
		claimedRewards.clear();

		for (int id : nbt.getIntArray("ClaimedRewards"))
		{
			claimedRewards.add((short) id);
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
					short[] as = new short[ai.length];

					for (int i = 0; i < ai.length; i++)
					{
						as[i] = (short) ai[i];
					}

					claimedPlayerRewards.put(player.getId(), new ShortOpenHashSet(as));
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
	public QuestTaskData getQuestTaskData(short task)
	{
		return taskData.get(task);
	}

	@Override
	public void unclaimReward(short reward)
	{
		QuestReward r = ServerQuestList.INSTANCE.getReward(reward);

		if (r == null)
		{
			return;
		}

		if (r.teamReward.getBoolean())
		{
			if (claimedRewards.rem(reward))
			{
				team.markDirty();
			}
		}
		else
		{
			Iterator<ShortCollection> itr = claimedPlayerRewards.values().iterator();

			while (itr.hasNext())
			{
				ShortCollection collection = itr.next();
				collection.rem(reward);

				if (collection.isEmpty())
				{
					itr.remove();
					team.markDirty();
				}
			}
		}
	}
}