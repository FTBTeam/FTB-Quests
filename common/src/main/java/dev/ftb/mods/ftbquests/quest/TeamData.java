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
import dev.ftb.mods.ftbquests.util.QuestKey;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class TeamData {
	public static TeamData get(Player player) {
		return FTBQuests.PROXY.getQuestFile(player.getCommandSenderWorld().isClientSide()).getData(player);
	}

	public final QuestFile file;
	public final UUID uuid;
	public String name;
	public boolean shouldSave;

	private final Long2ObjectOpenHashMap<TaskData> taskData;
	private final Map<QuestKey, Instant> claimedRewards;
	private boolean canEdit;
	private long money;
	private boolean autoPin;
	public final LongOpenHashSet pinnedQuests;

	private Long2ByteOpenHashMap progressCache;
	private Long2ByteOpenHashMap areDependenciesCompleteCache;

	public TeamData(QuestFile f, UUID id) {
		file = f;
		uuid = id;
		name = "";
		shouldSave = false;
		taskData = new Long2ObjectOpenHashMap<>();
		claimedRewards = new HashMap<>();
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
				FTBQuests.LOGGER.warn("Something's broken! Task data is null for team " + uuid + " but that team doesn't exist! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest);
				d = task.createData(this);
				taskData.put(task.id, d);
				return d;
			}

			throw new NullPointerException("Task data null! Task: " + task + ", Quest: " + task.quest.chapter.filename + ":" + task.quest + ", Team: " + name);
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

	@Nullable
	public Instant getRewardClaimTime(QuestKey key) {
		return claimedRewards.get(key);
	}

	public boolean isRewardClaimed(QuestKey key) {
		return getRewardClaimTime(key) != null;
	}

	public boolean isRewardClaimed(long id) {
		for (Map.Entry<QuestKey, Instant> entry : claimedRewards.entrySet()) {
			if (entry.getKey().id == id) {
				return true;
			}
		}

		return false;
	}

	public boolean claimReward(QuestKey key) {
		if (!claimedRewards.containsKey(key)) {
			Instant now = Instant.now();
			claimedRewards.put(key, now);
			claimedRewards.put(QuestKey.of(uuid, key.id), now);
			save();
			return true;
		}

		return false;
	}

	public boolean resetReward(long id) {
		if (claimedRewards.entrySet().removeIf(e -> e.getKey().id == id)) {
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

		if (nbt.contains("claimed_rewards", NbtType.INT_ARRAY) || nbt.contains("pinned_quests", NbtType.INT_ARRAY)) {
			oldData = true;

			for (int i : nbt.getIntArray("claimed_rewards")) {
				claimedRewards.add(i);
			}

			for (int i : nbt.getIntArray("pinned_quests")) {
				pinnedQuests.add(i);
			}
		} else if (nbt.contains("claimed_rewards", NbtType.LIST)) {
			oldData = true;
			ListTag cr = nbt.getList("claimed_rewards", NbtType.STRING);

			for (int i = 0; i < cr.size(); i++) {
				claimedRewards.add(file.getID(cr.getString(i)));
			}

			ListTag pq = nbt.getList("pinned_quests", NbtType.STRING);

			for (int i = 0; i < pq.size(); i++) {
				pinnedQuests.add(file.getID(pq.getString(i)));
			}
		} else {
			ListTag pq = nbt.getList("pinned_quests", NbtType.STRING);

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
			long now = Instant.now().toEpochMilli();
			buffer.writeVarInt(claimedRewards.size());

			for (Map.Entry<QuestKey, Instant> i : claimedRewards.entrySet()) {
				i.getKey().write(buffer);
				buffer.writeLong(now - i.getValue().toEpochMilli());
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
			long now = Instant.now().toEpochMilli();
			int crs = buffer.readVarInt();

			for (int i = 0; i < crs; i++) {
				QuestKey key = QuestKey.of(buffer);
				claimedRewards.put(key, Instant.ofEpochMilli(now - buffer.readLong()));
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
		QuestKey k = QuestKey.of(player.getUUID(), reward.id);

		if (claimReward(k)) {
			reward.claim(player, notify);

			if (file.isServerSide()) {
				new MessageClaimRewardResponse(k).sendToAll();
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