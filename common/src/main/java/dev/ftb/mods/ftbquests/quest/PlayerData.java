package dev.ftb.mods.ftbquests.quest;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.MessageClaimRewardResponse;
import dev.ftb.mods.ftbquests.net.MessageSyncEditingMode;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskData;
import dev.ftb.mods.ftbquests.util.OrderedCompoundTag;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
public class PlayerData {
	public static PlayerData get(Player player) {
		return FTBQuests.PROXY.getQuestFile(player.getCommandSenderWorld().isClientSide()).getData(player);
	}

	public final QuestFile file;
	public final UUID uuid;
	public String name;
	public boolean shouldSave;

	private final Long2ObjectOpenHashMap<TaskData> taskData;
	private final LongOpenHashSet claimedRewards;
	private boolean canEdit;
	private long money;
	private boolean autoPin;
	public final LongOpenHashSet pinnedQuests;

	private Long2ByteOpenHashMap progressCache;
	private Long2ByteOpenHashMap areDependenciesCompleteCache;

	public PlayerData(QuestFile f, UUID id) {
		file = f;
		uuid = id;
		name = "";
		shouldSave = false;
		taskData = new Long2ObjectOpenHashMap<>();
		claimedRewards = new LongOpenHashSet();
		canEdit = false;
		money = 0L;
		autoPin = false;
		pinnedQuests = new LongOpenHashSet();
	}

	public void save() {
		shouldSave = true;
	}

	public TaskData getTaskData(Task task) {
		TaskData d = taskData.get(task.id);

		if (d == null) {
			if (name.isEmpty()) {
				FTBQuests.LOGGER.warn("Something's broken! Task data is null for player " + uuid + " but that player doesn't exist! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest);
				d = task.createData(this);
				taskData.put(task.id, d);
				return d;
			}

			throw new NullPointerException("Task data null! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest + ", Player: " + name);
		}

		return d;
	}

	public void createTaskData(Task task, boolean strong) {
		if (strong || !taskData.containsKey(task.id)) {
			taskData.put(task.id, task.createData(this));
		}
	}

	public void removeTaskData(Task task) {
		taskData.remove(task.id);
	}

	public boolean isRewardClaimed(long id) {
		return claimedRewards.contains(id);
	}

	public boolean setRewardClaimed(long id, boolean claimed) {
		if (claimed ? claimedRewards.add(id) : claimedRewards.remove(id)) {
			save();
			return true;
		}

		return false;
	}

	public boolean getCanEdit() {
		return canEdit;
	}

	public boolean setCanEdit(boolean mode) {
		if (canEdit != mode) {
			canEdit = mode;
			save();

			if (file.isServerSide()) {
				ServerPlayer player = getPlayer();

				if (player != null) {
					new MessageSyncEditingMode(canEdit).sendTo(player);
				}
			}

			return true;
		}

		return false;
	}

	public long getMoney() {
		return money;
	}

	public void setMoney(long value) {
		long m = Math.max(0L, value);

		if (money != m) {
			money = m;
			save();
		}
	}

	public boolean getAutoPin() {
		return autoPin;
	}

	public void setAutoPin(boolean auto) {
		if (autoPin != auto) {
			autoPin = auto;
			save();
		}
	}

	public boolean isQuestPinned(long id) {
		return pinnedQuests.contains(id);
	}

	public void setQuestPinned(long id, boolean pinned) {
		if (pinned ? pinnedQuests.add(id) : pinnedQuests.remove(id)) {
			save();
		}
	}

	public void clearCache() {
		progressCache = null;
		areDependenciesCompleteCache = null;
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new OrderedCompoundTag();
		nbt.putString("uuid", uuid.toString());
		nbt.putString("name", name);
		nbt.putBoolean("can_edit", canEdit);
		nbt.putLong("money", money);
		nbt.putBoolean("auto_pin", autoPin);

		CompoundTag taskDataNBT = new OrderedCompoundTag();

		List<TaskData> taskDataList = new ArrayList<>(taskData.values());
		taskDataList.sort(Comparator.comparingLong(o -> o.task.id));

		for (TaskData data : taskDataList) {
			if (data.progress > 0L) {
				String key = QuestObjectBase.getCodeString(data.task.id);

				if (data.progress <= Integer.MAX_VALUE) {
					taskDataNBT.putInt(key, (int) data.progress);
				} else {
					taskDataNBT.putLong(key, data.progress);
				}
			}
		}

		nbt.put("task_progress", taskDataNBT);

		long[] claimedRewardsArray = claimedRewards.toLongArray();
		Arrays.sort(claimedRewardsArray);
		ListTag cr = new ListTag();

		for (long l : claimedRewardsArray) {
			cr.add(StringTag.valueOf(QuestObjectBase.getCodeString(l)));
		}

		nbt.put("claimed_rewards", cr);

		long[] pinnedQuestsArray = pinnedQuests.toLongArray();
		Arrays.sort(pinnedQuestsArray);
		ListTag pq = new ListTag();

		for (long l : pinnedQuestsArray) {
			pq.add(StringTag.valueOf(QuestObjectBase.getCodeString(l)));
		}

		nbt.put("pinned_quests", pq);

		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		name = nbt.getString("name");
		canEdit = nbt.getBoolean("can_edit");
		money = nbt.getLong("money");
		autoPin = nbt.getBoolean("auto_pin");

		boolean oldData = false;

		claimedRewards.clear();
		pinnedQuests.clear();

		if (nbt.contains("claimed_rewards", 11) || nbt.contains("pinned_quests", 11)) {
			oldData = true;

			for (int i : nbt.getIntArray("claimed_rewards")) {
				claimedRewards.add(i);
			}

			for (int i : nbt.getIntArray("pinned_quests")) {
				pinnedQuests.add(i);
			}
		} else {
			ListTag cr = nbt.getList("claimed_rewards", 8);

			for (int i = 0; i < cr.size(); i++) {
				claimedRewards.add(file.getID(cr.getString(i)));
			}

			ListTag pq = nbt.getList("pinned_quests", 8);

			for (int i = 0; i < pq.size(); i++) {
				pinnedQuests.add(file.getID(pq.getString(i)));
			}
		}

		CompoundTag taskDataNBT = nbt.getCompound("task_progress");

		for (String s : taskDataNBT.getAllKeys()) {
			Task task = file.getTask(oldData ? Integer.parseInt(s) : file.getID(s));

			if (task != null) {
				taskData.get(task.id).readProgress(taskDataNBT.getLong(s));
			}
		}

		if (oldData) {
			save();
		}
	}

	public void write(FriendlyByteBuf buffer, boolean self) {
		buffer.writeUtf(name, Short.MAX_VALUE);
		buffer.writeVarLong(money);
		int tds = 0;

		for (TaskData t : taskData.values()) {
			if (t.progress > 0L) {
				tds++;
			}
		}

		buffer.writeVarInt(tds);

		for (TaskData t : taskData.values()) {
			if (t.progress > 0L) {
				buffer.writeLong(t.task.id);
				buffer.writeVarLong(t.progress);
			}
		}

		if (self) {
			buffer.writeVarInt(claimedRewards.size());

			for (long i : claimedRewards) {
				buffer.writeLong(i);
			}

			buffer.writeBoolean(canEdit);
			buffer.writeBoolean(autoPin);

			buffer.writeVarInt(pinnedQuests.size());

			for (long reward : pinnedQuests) {
				buffer.writeLong(reward);
			}
		}
	}

	public void read(FriendlyByteBuf buffer, boolean self) {
		name = buffer.readUtf(Short.MAX_VALUE);
		money = buffer.readVarLong();

		int ts = buffer.readVarInt();

		for (int i = 0; i < ts; i++) {
			TaskData t = taskData.get(buffer.readLong());
			long progress = buffer.readVarLong();

			if (t != null) {
				t.progress = progress;
			}
		}

		claimedRewards.clear();
		canEdit = false;
		autoPin = false;
		pinnedQuests.clear();

		if (self) {
			int crs = buffer.readVarInt();

			for (int i = 0; i < crs; i++) {
				claimedRewards.add(buffer.readLong());
			}

			canEdit = buffer.readBoolean();
			autoPin = buffer.readBoolean();

			int pqs = buffer.readVarInt();

			for (int i = 0; i < pqs; i++) {
				pinnedQuests.add(buffer.readLong());
			}
		}
	}

	public int getRelativeProgress(QuestObject object) {
		if (!object.cacheProgress()) {
			return object.getRelativeProgressFromChildren(this);
		}

		if (progressCache == null) {
			progressCache = new Long2ByteOpenHashMap();
			progressCache.defaultReturnValue((byte) -1);
		}

		int i = progressCache.get(object.id);

		if (i == -1) {
			i = object.getRelativeProgressFromChildren(this);
			progressCache.put(object.id, (byte) i);
		}

		return i;
	}

	public boolean isStarted(QuestObject object) {
		return getRelativeProgress(object) > 0;
	}

	public boolean isComplete(QuestObject object) {
		return getRelativeProgress(object) >= 100;
	}

	public boolean areDependenciesComplete(Quest quest) {
		if (quest.dependencies.isEmpty()) {
			return true;
		}

		if (areDependenciesCompleteCache == null) {
			areDependenciesCompleteCache = new Long2ByteOpenHashMap();
			areDependenciesCompleteCache.defaultReturnValue((byte) -1);
		}

		byte b = areDependenciesCompleteCache.get(quest.id);

		if (b == -1) {
			b = areDependenciesComplete0(quest) ? (byte) 1 : (byte) 0;
			areDependenciesCompleteCache.put(quest.id, b);
		}

		return b == 1;
	}

	private boolean areDependenciesComplete0(Quest quest) {
		if (quest.minRequiredDependencies > 0) {
			int complete = 0;

			for (QuestObject dependency : quest.dependencies) {
				if (!dependency.invalid && isComplete(dependency)) {
					complete++;

					if (complete >= quest.minRequiredDependencies) {
						return true;
					}
				}
			}

			return false;
		}

		if (quest.dependencyRequirement.one) {
			for (QuestObject object : quest.dependencies) {
				if (!object.invalid && (quest.dependencyRequirement.completed ? isComplete(object) : isStarted(object))) {
					return true;
				}
			}

			return false;
		}

		for (QuestObject object : quest.dependencies) {
			if (!object.invalid && (quest.dependencyRequirement.completed ? !isComplete(object) : !isStarted(object))) {
				return false;
			}
		}

		return true;
	}

	public boolean canStartTasks(Quest quest) {
		return areDependenciesComplete(quest);
	}

	public boolean hasUnclaimedRewards() {
		for (ChapterGroup group : file.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				if (hasUnclaimedRewards(chapter)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasUnclaimedRewards(Chapter chapter) {
		for (Quest quest : chapter.quests) {
			if (hasUnclaimedRewards(quest)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasUnclaimedRewards(Quest quest) {
		if (isComplete(quest)) {
			for (Reward reward : quest.rewards) {
				if (getClaimType(reward) == RewardClaimType.CAN_CLAIM) {
					return true;
				}
			}
		}

		return false;
	}

	public void claimReward(ServerPlayer player, Reward reward, boolean notify) {
		if (setRewardClaimed(reward.id, true)) {
			reward.claim(player, notify);

			if (file.isServerSide()) {
				new MessageClaimRewardResponse(uuid, reward.id, 1).sendToAll();
			}
		}
	}

	@Nullable
	public ServerPlayer getPlayer() {
		return ((ServerQuestFile) file).server.getPlayerList().getPlayer(uuid);
	}

	public List<ServerPlayer> getOnlineMembers() {
		ServerPlayer playerEntity = getPlayer();

		if (playerEntity != null) {
			return Collections.singletonList(playerEntity);
		}

		return Collections.emptyList();
	}

	public void checkAutoCompletion(Quest quest) {
		if (quest.rewards.isEmpty() || !isComplete(quest)) {
			return;
		}

		List<ServerPlayer> online = null;

		for (Reward reward : quest.rewards) {
			RewardAutoClaim auto = reward.getAutoClaimType();

			if (auto != RewardAutoClaim.DISABLED) {
				if (online == null) {
					online = getOnlineMembers();

					if (online.isEmpty()) {
						return;
					}
				}

				for (ServerPlayer player : online) {
					claimReward(player, reward, auto == RewardAutoClaim.ENABLED);
				}
			}
		}
	}

	public RewardClaimType getClaimType(Reward reward) {
		boolean r = isRewardClaimed(reward.id);

		if (r) {
			return RewardClaimType.CLAIMED;
		} else if (isComplete(reward.quest)) {
			return RewardClaimType.CAN_CLAIM;
		}

		return RewardClaimType.CANT_CLAIM;
	}
}