package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageClaimRewardResponse;
import com.feed_the_beast.ftbquests.net.MessageSyncEditingMode;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardClaimType;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.util.OrderedCompoundNBT;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class PlayerData
{
	public static PlayerData get(PlayerEntity player)
	{
		return FTBQuests.PROXY.getQuestFile(player.getEntityWorld()).getData(player);
	}

	public final QuestFile file;
	public final UUID uuid;
	public String name;
	public boolean shouldSave;

	private final Int2ObjectOpenHashMap<TaskData> taskData;
	private final IntOpenHashSet claimedRewards;
	private boolean canEdit;
	private long money;
	private boolean autoPin;
	public final IntOpenHashSet pinnedQuests;

	private Int2ByteOpenHashMap progressCache;
	private Int2ByteOpenHashMap areDependenciesCompleteCache;

	public PlayerData(QuestFile f, UUID id)
	{
		file = f;
		uuid = id;
		name = "";
		shouldSave = false;
		taskData = new Int2ObjectOpenHashMap<>();
		claimedRewards = new IntOpenHashSet();
		canEdit = false;
		money = 0L;
		autoPin = false;
		pinnedQuests = new IntOpenHashSet();
	}

	public void save()
	{
		shouldSave = true;
	}

	public TaskData getTaskData(Task task)
	{
		TaskData d = taskData.get(task.id);

		if (d == null)
		{
			if (name.isEmpty())
			{
				FTBQuests.LOGGER.warn("Something's broken! Task data is null for player " + uuid + " but that player doesn't exist! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest);
				d = task.createData(this);
				taskData.put(task.id, d);
				return d;
			}

			throw new NullPointerException("Task data null! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest + ", Player: " + name);
		}

		return d;
	}

	public void createTaskData(Task task, boolean strong)
	{
		if (strong || !taskData.containsKey(task.id))
		{
			taskData.put(task.id, task.createData(this));
		}
	}

	public void removeTaskData(Task task)
	{
		taskData.remove(task.id);
	}

	public boolean isRewardClaimed(int id)
	{
		return claimedRewards.contains(id);
	}

	public boolean setRewardClaimed(int id, boolean claimed)
	{
		if (claimed ? claimedRewards.add(id) : claimedRewards.remove(id))
		{
			save();
			return true;
		}

		return false;
	}

	public boolean getCanEdit()
	{
		return canEdit;
	}

	public boolean setCanEdit(boolean mode)
	{
		if (canEdit != mode)
		{
			canEdit = mode;
			save();

			if (file.getSide().isServer())
			{
				ServerPlayerEntity player = getPlayer();

				if (player != null)
				{
					new MessageSyncEditingMode(canEdit).sendTo(player);
				}
			}

			return true;
		}

		return false;
	}

	public long getMoney()
	{
		return money;
	}

	public void setMoney(long value)
	{
		long m = Math.max(0L, value);

		if (money != m)
		{
			money = m;
			save();
		}
	}

	public boolean getAutoPin()
	{
		return autoPin;
	}

	public void setAutoPin(boolean auto)
	{
		if (autoPin != auto)
		{
			autoPin = auto;
			save();
		}
	}

	public boolean isQuestPinned(int id)
	{
		return pinnedQuests.contains(id);
	}

	public void setQuestPinned(int id, boolean pinned)
	{
		if (pinned ? pinnedQuests.add(id) : pinnedQuests.remove(id))
		{
			save();
		}
	}

	public void clearCache()
	{
		progressCache = null;
		areDependenciesCompleteCache = null;
	}

	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new OrderedCompoundNBT();
		nbt.putString("uuid", uuid.toString());
		nbt.putString("name", name);
		nbt.putBoolean("can_edit", canEdit);
		nbt.putLong("money", money);
		nbt.putBoolean("auto_pin", autoPin);

		CompoundNBT taskDataNBT = new OrderedCompoundNBT();

		List<TaskData> taskDataList = new ArrayList<>(taskData.values());
		taskDataList.sort(Comparator.comparingInt(o -> o.task.id));

		for (TaskData data : taskDataList)
		{
			if (data.progress > 0L)
			{
				if (data.progress <= Integer.MAX_VALUE)
				{
					taskDataNBT.putInt(QuestObjectBase.getCodeString(data.task), (int) data.progress);
				}
				else
				{
					taskDataNBT.putLong(QuestObjectBase.getCodeString(data.task), data.progress);
				}
			}
		}

		nbt.put("task_progress", taskDataNBT);

		int[] claimedRewardsArray = claimedRewards.toIntArray();
		Arrays.sort(claimedRewardsArray);
		nbt.putIntArray("claimed_rewards", claimedRewardsArray);

		int[] pinnedQuestsArray = pinnedQuests.toIntArray();
		Arrays.sort(pinnedQuestsArray);
		nbt.putIntArray("pinned_quests", pinnedQuestsArray);

		return nbt;
	}

	public void deserializeNBT(CompoundNBT nbt)
	{
		name = nbt.getString("name");
		canEdit = nbt.getBoolean("can_edit");
		money = nbt.getLong("money");
		autoPin = nbt.getBoolean("auto_pin");

		CompoundNBT taskDataNBT = nbt.getCompound("task_progress");

		for (String s : taskDataNBT.keySet())
		{
			Task task = file.getTask(file.getID(s));

			if (task != null)
			{
				taskData.get(task.id).readProgress(taskDataNBT.getLong(s));
			}
		}

		claimedRewards.clear();
		claimedRewards.addAll(new IntOpenHashSet(nbt.getIntArray("claimed_rewards")));
		pinnedQuests.clear();
		pinnedQuests.addAll(new IntOpenHashSet(nbt.getIntArray("pinned_quests")));
	}

	public void write(PacketBuffer buffer, boolean self)
	{
		buffer.writeString(name, Short.MAX_VALUE);
		buffer.writeVarLong(money);
		int tds = 0;

		for (TaskData t : taskData.values())
		{
			if (t.progress > 0L)
			{
				tds++;
			}
		}

		buffer.writeVarInt(tds);

		for (TaskData t : taskData.values())
		{
			if (t.progress > 0L)
			{
				buffer.writeVarInt(t.task.id);
				buffer.writeVarLong(t.progress);
			}
		}

		if (self)
		{
			buffer.writeVarInt(claimedRewards.size());

			for (int i : claimedRewards)
			{
				buffer.writeVarInt(i);
			}

			buffer.writeBoolean(canEdit);
			buffer.writeBoolean(autoPin);

			buffer.writeVarInt(pinnedQuests.size());

			for (Integer reward : pinnedQuests)
			{
				buffer.writeVarInt(reward);
			}
		}
	}

	public void read(PacketBuffer buffer, boolean self)
	{
		name = buffer.readString(Short.MAX_VALUE);
		money = buffer.readVarLong();

		int ts = buffer.readVarInt();

		for (int i = 0; i < ts; i++)
		{
			TaskData t = taskData.get(buffer.readVarInt());
			long progress = buffer.readVarLong();

			if (t != null)
			{
				t.progress = progress;
			}
		}

		claimedRewards.clear();
		canEdit = false;
		autoPin = false;
		pinnedQuests.clear();

		if (self)
		{
			int crs = buffer.readVarInt();

			for (int i = 0; i < crs; i++)
			{
				claimedRewards.add(buffer.readVarInt());
			}

			canEdit = buffer.readBoolean();
			autoPin = buffer.readBoolean();

			int pqs = buffer.readVarInt();

			for (int i = 0; i < pqs; i++)
			{
				pinnedQuests.add(buffer.readVarInt());
			}
		}
	}

	public int getRelativeProgress(QuestObject object)
	{
		if (!object.cacheProgress())
		{
			return object.getRelativeProgressFromChildren(this);
		}

		if (progressCache == null)
		{
			progressCache = new Int2ByteOpenHashMap();
			progressCache.defaultReturnValue((byte) -1);
		}

		int i = progressCache.get(object.id);

		if (i == -1)
		{
			i = object.getRelativeProgressFromChildren(this);
			progressCache.put(object.id, (byte) i);
		}

		return i;
	}

	public boolean isStarted(QuestObject object)
	{
		return getRelativeProgress(object) > 0;
	}

	public boolean isComplete(QuestObject object)
	{
		return getRelativeProgress(object) >= 100;
	}

	public boolean areDependenciesComplete(Quest quest)
	{
		if (quest.dependencies.isEmpty())
		{
			return true;
		}

		if (areDependenciesCompleteCache == null)
		{
			areDependenciesCompleteCache = new Int2ByteOpenHashMap();
			areDependenciesCompleteCache.defaultReturnValue((byte) -1);
		}

		byte b = areDependenciesCompleteCache.get(quest.id);

		if (b == -1)
		{
			b = areDependenciesComplete0(quest) ? (byte) 1 : (byte) 0;
			areDependenciesCompleteCache.put(quest.id, b);
		}

		return b == 1;
	}

	private boolean areDependenciesComplete0(Quest quest)
	{
		if (quest.minRequiredDependencies > 0)
		{
			int complete = 0;

			for (QuestObject dependency : quest.dependencies)
			{
				if (!dependency.invalid && isComplete(dependency))
				{
					complete++;

					if (complete >= quest.minRequiredDependencies)
					{
						return true;
					}
				}
			}

			return false;
		}

		if (quest.dependencyRequirement.one)
		{
			for (QuestObject object : quest.dependencies)
			{
				if (!object.invalid && (quest.dependencyRequirement.completed ? isComplete(object) : isStarted(object)))
				{
					return true;
				}
			}

			return false;
		}

		for (QuestObject object : quest.dependencies)
		{
			if (!object.invalid && (quest.dependencyRequirement.completed ? !isComplete(object) : !isStarted(object)))
			{
				return false;
			}
		}

		return true;
	}

	public boolean canStartTasks(Quest quest)
	{
		return areDependenciesComplete(quest);
	}

	public boolean hasUnclaimedRewards()
	{
		for (Chapter chapter : file.chapters)
		{
			if (hasUnclaimedRewards(chapter))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasUnclaimedRewards(Chapter chapter)
	{
		for (Quest quest : chapter.quests)
		{
			if (hasUnclaimedRewards(quest))
			{
				return true;
			}
		}

		for (Chapter chapter1 : chapter.getChildren())
		{
			if (hasUnclaimedRewards(chapter1))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasUnclaimedRewards(Quest quest)
	{
		if (isComplete(quest))
		{
			for (Reward reward : quest.rewards)
			{
				if (getClaimType(reward) == RewardClaimType.CAN_CLAIM)
				{
					return true;
				}
			}
		}

		return false;
	}

	public void claimReward(ServerPlayerEntity player, Reward reward, boolean notify)
	{
		if (setRewardClaimed(reward.id, true))
		{
			reward.claim(player, notify);

			if (file.getSide().isServer())
			{
				new MessageClaimRewardResponse(uuid, reward.id, 1).sendToAll();
			}
		}
	}

	@Nullable
	public ServerPlayerEntity getPlayer()
	{
		return ((ServerQuestFile) file).server.getPlayerList().getPlayerByUUID(uuid);
	}

	public List<ServerPlayerEntity> getOnlineMembers()
	{
		ServerPlayerEntity playerEntity = getPlayer();

		if (playerEntity != null)
		{
			return Collections.singletonList(playerEntity);
		}

		return Collections.emptyList();
	}

	public void checkAutoCompletion(Quest quest)
	{
		if (quest.rewards.isEmpty() || !isComplete(quest))
		{
			return;
		}

		List<ServerPlayerEntity> online = null;

		for (Reward reward : quest.rewards)
		{
			RewardAutoClaim auto = reward.getAutoClaimType();

			if (auto != RewardAutoClaim.DISABLED)
			{
				if (online == null)
				{
					online = getOnlineMembers();

					if (online.isEmpty())
					{
						return;
					}
				}

				for (ServerPlayerEntity player : online)
				{
					claimReward(player, reward, auto == RewardAutoClaim.ENABLED);
				}
			}
		}
	}

	public RewardClaimType getClaimType(Reward reward)
	{
		boolean r = isRewardClaimed(reward.id);

		if (r)
		{
			return RewardClaimType.CLAIMED;
		}
		else if (isComplete(reward.quest))
		{
			return RewardClaimType.CAN_CLAIM;
		}

		return RewardClaimType.CANT_CLAIM;
	}
}