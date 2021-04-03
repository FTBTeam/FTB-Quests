package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.MessageClaimRewardResponse;
import dev.ftb.mods.ftbquests.net.MessageObjectCompleted;
import dev.ftb.mods.ftbquests.net.MessageObjectCompletedReset;
import dev.ftb.mods.ftbquests.net.MessageObjectStarted;
import dev.ftb.mods.ftbquests.net.MessageObjectStartedReset;
import dev.ftb.mods.ftbquests.net.MessageSyncEditingMode;
import dev.ftb.mods.ftbquests.net.MessageUpdateTaskProgress;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.OrderedCompoundTag;
import dev.ftb.mods.ftbquests.util.QuestKey;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class TeamData {
	public static int VERSION = 1;
	private static final byte BOOL_NONE = -1;
	private static final byte BOOL_FALSE = 0;
	private static final byte BOOL_TRUE = 1;
	private static final Comparator<Long2LongMap.Entry> LONG2LONG_COMPARATOR = (e1, e2) -> Long.compareUnsigned(e1.getLongValue(), e2.getLongValue());
	private static final Comparator<Object2LongMap.Entry<QuestKey>> OBJECT2LONG_COMPARATOR = (e1, e2) -> Long.compareUnsigned(e1.getLongValue(), e2.getLongValue());

	public static TeamData get(Player player) {
		return FTBQuests.PROXY.getQuestFile(player.getCommandSenderWorld().isClientSide()).getData(player);
	}

	public final QuestFile file;
	public final UUID uuid;
	public String name;
	public boolean shouldSave;

	private final Long2LongOpenHashMap taskProgress;
	private final Object2LongOpenHashMap<QuestKey> claimedRewards;
	private final Long2LongOpenHashMap started;
	private final Long2LongOpenHashMap completed;
	private boolean canEdit;
	private long money;
	private boolean autoPin;
	public final LongOpenHashSet pinnedQuests;

	private Long2ByteOpenHashMap areDependenciesCompleteCache;
	private Object2ByteOpenHashMap<QuestKey> unclaimedRewardsCache;

	public TeamData(QuestFile f, UUID id) {
		file = f;
		uuid = id;
		name = "";
		shouldSave = false;
		taskProgress = new Long2LongOpenHashMap();
		taskProgress.defaultReturnValue(0L);
		claimedRewards = new Object2LongOpenHashMap<>();
		claimedRewards.defaultReturnValue(0L);
		started = new Long2LongOpenHashMap();
		started.defaultReturnValue(0L);
		completed = new Long2LongOpenHashMap();
		completed.defaultReturnValue(0L);
		canEdit = false;
		money = 0L;
		autoPin = false;
		pinnedQuests = new LongOpenHashSet();
	}

	public void save() {
		shouldSave = true;
	}

	@Override
	public String toString() {
		return name.isEmpty() ? uuid.toString() : name;
	}

	public long getProgress(long task) {
		return taskProgress.get(task);
	}

	public long getProgress(Task task) {
		return getProgress(task.id);
	}

	@Nullable
	public Date getStartedTime(long id) {
		long t = started.get(id);
		return t == 0L ? null : new Date(t);
	}

	public boolean setStarted(long id, @Nullable Date time) {
		if (time == null) {
			if (started.remove(id) >= 0L) {
				clearCachedProgress();
				save();

				if (ChangeProgress.sendUpdates && file.isServerSide()) {
					new MessageObjectStartedReset(uuid, id).sendToAll();
				}

				return true;
			}
		} else {
			if (started.put(id, time.getTime()) == 0L) {
				clearCachedProgress();
				save();

				if (ChangeProgress.sendUpdates && file.isServerSide()) {
					new MessageObjectStarted(uuid, id).sendToAll();
				}

				return true;
			}
		}

		return false;
	}

	@Nullable
	public Date getCompletedTime(long id) {
		long t = completed.get(id);
		return t == 0L ? null : new Date(t);
	}

	public boolean setCompleted(long id, @Nullable Date time) {
		if (time == null) {
			if (completed.remove(id) >= 0L) {
				clearCachedProgress();
				save();

				if (ChangeProgress.sendUpdates && file.isServerSide()) {
					new MessageObjectCompletedReset(uuid, id).sendToAll();
				}

				return true;
			}
		} else {
			if (completed.put(id, time.getTime()) == 0L) {
				clearCachedProgress();
				save();

				if (ChangeProgress.sendUpdates && file.isServerSide()) {
					new MessageObjectCompleted(uuid, id).sendToAll();
				}

				return true;
			}
		}

		return false;
	}

	@Nullable
	public Date getRewardClaimTime(UUID player, Reward reward) {
		QuestKey key = QuestKey.of(reward.isTeamReward() ? Util.NIL_UUID : player, reward.id);
		long t = claimedRewards.getLong(key);
		return t == 0L ? null : new Date(t);
	}

	public boolean isRewardClaimed(UUID player, Reward reward) {
		return getRewardClaimTime(player, reward) != null;
	}

	public boolean hasUnclaimedRewards(UUID player, QuestObject object) {
		if (unclaimedRewardsCache == null) {
			unclaimedRewardsCache = new Object2ByteOpenHashMap<>();
			unclaimedRewardsCache.defaultReturnValue(BOOL_NONE);
		}

		QuestKey key = QuestKey.of(player, object.id);
		byte b = unclaimedRewardsCache.getByte(key);

		if (b == -1) {
			b = object.hasUnclaimedRewardsRaw(this, player) ? BOOL_TRUE : BOOL_FALSE;
			unclaimedRewardsCache.put(key, b);
		}

		return b == BOOL_TRUE;
	}

	public boolean claimReward(UUID player, Reward reward) {
		QuestKey key = QuestKey.of(reward.isTeamReward() ? Util.NIL_UUID : player, reward.id);

		if (!claimedRewards.containsKey(key)) {
			claimedRewards.put(key, System.currentTimeMillis());
			clearCachedProgress();
			save();
			return true;
		}

		return false;
	}

	public boolean resetReward(Reward reward) {
		if (claimedRewards.object2LongEntrySet().removeIf(e -> e.getKey().id == reward.id)) {
			clearCachedProgress();
			save();
			return true;
		}

		return false;
	}

	public boolean resetReward(UUID player, Reward reward) {
		if (claimedRewards.removeLong(QuestKey.of(reward.isTeamReward() ? Util.NIL_UUID : player, reward.id)) != 0L) {
			clearCachedProgress();
			save();
			return true;
		}

		return false;
	}

	public boolean setRewardClaimed(UUID player, Reward reward, Date time) {
		if (claimedRewards.put(QuestKey.of(reward.isTeamReward() ? Util.NIL_UUID : player, reward.id), time.getTime()) != 0L) {
			clearCachedProgress();
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
			clearCachedProgress();
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

	public void clearCachedProgress() {
		areDependenciesCompleteCache = null;
		unclaimedRewardsCache = null;
	}

	public CompoundTag serializeNBT() {
		CompoundTag nbt = new OrderedCompoundTag();
		nbt.putInt("version", VERSION);
		nbt.putString("uuid", UUIDTypeAdapter.fromUUID(uuid));
		nbt.putString("name", name);
		nbt.putBoolean("can_edit", canEdit);
		nbt.putLong("money", money);
		nbt.putBoolean("auto_pin", autoPin);

		CompoundTag taskProgressNBT = new OrderedCompoundTag();

		for (Long2LongMap.Entry entry : taskProgress.long2LongEntrySet()) {
			if (entry.getLongValue() <= Integer.MAX_VALUE) {
				taskProgressNBT.putInt(QuestObjectBase.getCodeString(entry.getLongKey()), (int) entry.getLongValue());
			} else {
				taskProgressNBT.putLong(QuestObjectBase.getCodeString(entry.getLongKey()), entry.getLongValue());
			}
		}

		nbt.put("task_progress", taskProgressNBT);

		CompoundTag startedNBT = new OrderedCompoundTag();

		for (Long2LongMap.Entry entry : started.long2LongEntrySet().stream().sorted(LONG2LONG_COMPARATOR).collect(Collectors.toList())) {
			startedNBT.putString(QuestObjectBase.getCodeString(entry.getLongKey()), Instant.ofEpochMilli(entry.getLongValue()).toString());
		}

		nbt.put("started", startedNBT);

		CompoundTag completedNBT = new OrderedCompoundTag();

		for (Long2LongMap.Entry entry : completed.long2LongEntrySet().stream().sorted(LONG2LONG_COMPARATOR).collect(Collectors.toList())) {
			completedNBT.putString(QuestObjectBase.getCodeString(entry.getLongKey()), Instant.ofEpochMilli(entry.getLongValue()).toString());
		}

		nbt.put("completed", completedNBT);

		CompoundTag claimedRewardsNBT = new OrderedCompoundTag();

		for (Object2LongMap.Entry<QuestKey> entry : claimedRewards.object2LongEntrySet().stream().sorted(OBJECT2LONG_COMPARATOR).collect(Collectors.toList())) {
			claimedRewardsNBT.putString(entry.getKey().toString(), Instant.ofEpochMilli(entry.getLongValue()).toString());
		}

		nbt.put("claimed_rewards", claimedRewardsNBT);

		long[] pinnedQuestsArray = pinnedQuests.toLongArray();
		Arrays.sort(pinnedQuestsArray);
		ListTag pinnedQuestsNBT = new ListTag();

		for (long l : pinnedQuestsArray) {
			pinnedQuestsNBT.add(StringTag.valueOf(QuestObjectBase.getCodeString(l)));
		}

		nbt.put("pinned_quests", pinnedQuestsNBT);

		return nbt;
	}

	public void deserializeNBT(CompoundTag nbt) {
		int fileVersion = nbt.getInt("version");

		if (fileVersion != VERSION) {
			save();
		}

		name = nbt.getString("name");
		canEdit = nbt.getBoolean("can_edit");
		money = nbt.getLong("money");
		autoPin = nbt.getBoolean("auto_pin");

		taskProgress.clear();
		claimedRewards.clear();
		pinnedQuests.clear();

		CompoundTag claimedRewardsNBT = nbt.getCompound("claimed_rewards");

		for (String s : claimedRewardsNBT.getAllKeys()) {
			claimedRewards.put(QuestKey.of(s), Instant.parse(claimedRewardsNBT.getString(s)).toEpochMilli());
		}

		ListTag pinnedQuestsNBT = nbt.getList("pinned_quests", NbtType.STRING);

		for (int i = 0; i < pinnedQuestsNBT.size(); i++) {
			pinnedQuests.add(file.getID(pinnedQuestsNBT.getString(i)));
		}

		CompoundTag taskProgressNBT = nbt.getCompound("task_progress");

		for (String s : taskProgressNBT.getAllKeys()) {
			taskProgress.put(file.getID(s), taskProgressNBT.getLong(s));
		}

		CompoundTag startedNBT = nbt.getCompound("started");

		for (String s : startedNBT.getAllKeys()) {
			started.put(file.getID(s), Instant.parse(startedNBT.getString(s)).toEpochMilli());
		}

		CompoundTag completedNBT = nbt.getCompound("completed");

		for (String s : completedNBT.getAllKeys()) {
			completed.put(file.getID(s), Instant.parse(completedNBT.getString(s)).toEpochMilli());
		}
	}

	public void write(FriendlyByteBuf buffer, boolean self) {
		buffer.writeUtf(name, Short.MAX_VALUE);
		buffer.writeVarLong(money);
		buffer.writeVarInt(taskProgress.size());

		for (Long2LongMap.Entry entry : taskProgress.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(entry.getLongValue());
		}

		long now = System.currentTimeMillis();

		buffer.writeVarInt(started.size());

		for (Long2LongOpenHashMap.Entry entry : started.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(now - entry.getValue());
		}

		buffer.writeVarInt(completed.size());

		for (Long2LongOpenHashMap.Entry entry : completed.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(now - entry.getValue());
		}

		if (self) {
			buffer.writeVarInt(claimedRewards.size());

			for (Object2LongMap.Entry<QuestKey> entry : claimedRewards.object2LongEntrySet()) {
				entry.getKey().write(buffer);
				buffer.writeVarLong(now - entry.getLongValue());
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

		taskProgress.clear();
		int ts = buffer.readVarInt();

		for (int i = 0; i < ts; i++) {
			taskProgress.put(buffer.readLong(), buffer.readVarLong());
		}

		long now = System.currentTimeMillis();

		started.clear();

		int ss = buffer.readVarInt();

		for (int i = 0; i < ss; i++) {
			started.put(buffer.readLong(), now - buffer.readVarLong());
		}

		completed.clear();

		int cs = buffer.readVarInt();

		for (int i = 0; i < cs; i++) {
			completed.put(buffer.readLong(), now - buffer.readVarLong());
		}

		claimedRewards.clear();
		canEdit = false;
		autoPin = false;
		pinnedQuests.clear();

		if (self) {
			int crs = buffer.readVarInt();

			for (int i = 0; i < crs; i++) {
				QuestKey key = QuestKey.of(buffer);
				claimedRewards.put(key, now - buffer.readVarLong());
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
		if (isCompleted(object)) {
			return 100;
		} else if (!isStarted(object)) {
			return 0;
		}

		//} else if (!object.cacheProgress()) {
		//  return object.getRelativeProgressFromChildren(this);
		//}

		// FIXME?
		return object.getRelativeProgressFromChildren(this);
	}

	public boolean isStarted(QuestObject object) {
		return started.containsKey(object.id);
	}

	public boolean isCompleted(QuestObject object) {
		return completed.containsKey(object.id);
	}

	public boolean areDependenciesComplete(Quest quest) {
		if (quest.dependencies.isEmpty()) {
			return true;
		}

		if (areDependenciesCompleteCache == null) {
			areDependenciesCompleteCache = new Long2ByteOpenHashMap();
			areDependenciesCompleteCache.defaultReturnValue(BOOL_NONE);
		}

		byte b = areDependenciesCompleteCache.get(quest.id);

		if (b == -1) {
			b = areDependenciesComplete0(quest) ? BOOL_TRUE : BOOL_FALSE;
			areDependenciesCompleteCache.put(quest.id, b);
		}

		return b == BOOL_TRUE;
	}

	private boolean areDependenciesComplete0(Quest quest) {
		if (quest.minRequiredDependencies > 0) {
			int complete = 0;

			for (QuestObject dependency : quest.dependencies) {
				if (!dependency.invalid && isCompleted(dependency)) {
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
				if (!object.invalid && (quest.dependencyRequirement.completed ? isCompleted(object) : isStarted(object))) {
					return true;
				}
			}

			return false;
		}

		for (QuestObject object : quest.dependencies) {
			if (!object.invalid && (quest.dependencyRequirement.completed ? !isCompleted(object) : !isStarted(object))) {
				return false;
			}
		}

		return true;
	}

	public boolean canStartTasks(Quest quest) {
		return areDependenciesComplete(quest);
	}

	public void claimReward(ServerPlayer player, Reward reward, boolean notify) {
		if (claimReward(player.getUUID(), reward)) {
			reward.claim(player, notify);

			if (file.isServerSide()) {
				new MessageClaimRewardResponse(uuid, player.getUUID(), reward.id).sendToAll();
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
		if (quest.rewards.isEmpty() || !isCompleted(quest)) {
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

	public RewardClaimType getClaimType(UUID player, Reward reward) {
		boolean r = isRewardClaimed(player, reward);

		if (r) {
			return RewardClaimType.CLAIMED;
		} else if (isCompleted(reward.quest)) {
			return RewardClaimType.CAN_CLAIM;
		}

		return RewardClaimType.CANT_CLAIM;
	}

	public void resetProgress(Task task) {
		if (taskProgress.remove(task.id) > 0L) {
			save();
		}
	}

	public final void setProgress(Task task, long progress) {
		long maxProgress = task.getMaxProgress();
		progress = Math.max(0L, Math.min(progress, maxProgress));
		long prevProgress = getProgress(task);

		if (prevProgress != progress) {
			if (progress == 0L) {
				taskProgress.remove(task.id);
			} else {
				taskProgress.put(task.id, progress);
			}

			clearCachedProgress();

			if (file.isServerSide()) {
				Date now = new Date();

				if (ChangeProgress.sendUpdates) {
					new MessageUpdateTaskProgress(this, task.id, progress).sendToAll();
				}

				if (prevProgress == 0L) {
					task.onStarted(new QuestProgressEventData<>(now, this, task, getOnlineMembers(), Collections.emptyList()));
				}

				if (progress >= maxProgress) {
					List<ServerPlayer> onlineMembers = getOnlineMembers();
					List<ServerPlayer> notifiedPlayers;

					if (!task.quest.chapter.alwaysInvisible && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates)) {
						notifiedPlayers = onlineMembers;
					} else {
						notifiedPlayers = Collections.emptyList();
					}

					task.onCompleted(new QuestProgressEventData<>(now, this, task, onlineMembers, notifiedPlayers));

					for (ServerPlayer player : onlineMembers) {
						FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, task.id);
					}
				}
			}

			save();
		}
	}

	public final void addProgress(Task task, long p) {
		setProgress(task, getProgress(task) + p);
	}
}