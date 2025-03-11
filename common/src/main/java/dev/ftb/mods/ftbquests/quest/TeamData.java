package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UndashedUuid;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.QuestKey;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TeamData {
	public static final int VERSION = 1;
	public static final int AUTO_PIN_ID = 1;

	private static final byte BOOL_UNKNOWN = -1;
	private static final byte BOOL_FALSE = 0;
	private static final byte BOOL_TRUE = 1;

	private static final Comparator<Long2LongMap.Entry> LONG2LONG_COMPARATOR = (e1, e2) -> Long.compareUnsigned(e1.getLongValue(), e2.getLongValue());
	private static final Comparator<Object2LongMap.Entry<QuestKey>> OBJECT2LONG_COMPARATOR = (e1, e2) -> Long.compareUnsigned(e1.getLongValue(), e2.getLongValue());

	public static final StreamCodec<FriendlyByteBuf,TeamData> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public TeamData decode(FriendlyByteBuf buf) {
			return Util.make(new TeamData(buf.readUUID(), ClientQuestFile.INSTANCE), d -> d.readNetData(buf));
		}

		@Override
		public void encode(FriendlyByteBuf buf, TeamData data) {
			buf.writeUUID(data.getTeamId());
			data.write(buf);
		}
	};

	private final UUID teamId;
	private final BaseQuestFile file;

	private String name;
	private boolean shouldSave;
	private boolean locked;
	private boolean rewardsBlocked;

	private final Long2LongMap taskProgress;
	private final Object2LongMap<QuestKey> claimedRewards;
	private final Long2LongMap started;
	private final Long2LongMap completed;
	private final Object2ObjectMap<UUID,PerPlayerData> perPlayerData;

	private final Long2ByteMap areDependenciesCompleteCache;
	private final Long2ByteMap areDependenciesVisibleCache;
	private final Object2ByteMap<QuestKey> unclaimedRewardsCache;
	private final Long2BooleanMap exclusionCache;

	public TeamData(UUID teamId, BaseQuestFile file) {
		this(teamId, file, "");
	}

	public TeamData(UUID teamId, BaseQuestFile file, String name) {
		this.teamId = teamId;
		this.file = file;
		this.name = name;

		shouldSave = false;

		taskProgress = new Long2LongOpenHashMap();
		taskProgress.defaultReturnValue(0L);
		claimedRewards = new Object2LongOpenHashMap<>();
		claimedRewards.defaultReturnValue(0L);
		started = new Long2LongOpenHashMap();
		started.defaultReturnValue(0L);
		completed = new Long2LongOpenHashMap();
		completed.defaultReturnValue(0L);
		perPlayerData = new Object2ObjectOpenHashMap<>();
		areDependenciesCompleteCache = new Long2ByteOpenHashMap();
		areDependenciesVisibleCache = new Long2ByteOpenHashMap();
		unclaimedRewardsCache = new Object2ByteOpenHashMap<>();
		exclusionCache = new Long2BooleanOpenHashMap();
	}

	public UUID getTeamId() {
		return teamId;
	}

	public BaseQuestFile getFile() {
		return file;
	}

	@NotNull
	public static TeamData get(Player player) {
		return FTBQuestsAPI.api().getQuestFile(player.getCommandSenderWorld().isClientSide()).getTeamData(player).orElseThrow();
	}

	public void markDirty() {
		shouldSave = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			markDirty();
		}
	}

	public void saveIfChanged() {
		if (shouldSave && file instanceof ServerQuestFile sqf) {
			Path path = sqf.server.getWorldPath(ServerQuestFile.FTBQUESTS_DATA);
			SNBT.write(path.resolve(teamId + ".snbt"), serializeNBT());
			shouldSave = false;
		}
	}

	@Override
	public String toString() {
		return name.isEmpty() ? teamId.toString() : name;
	}

	public long getProgress(long taskId) {
		return taskProgress.get(taskId);
	}

	public long getProgress(Task task) {
		return getProgress(task.id);
	}

	public Optional<Date> getStartedTime(long questId) {
		long when = started.get(questId);
		return when == 0L ? Optional.empty() : Optional.of(new Date(when));
	}

	public boolean setStarted(long questId, @Nullable Date time) {
		if (!locked) {
			if (time == null) {
				if (started.remove(questId) >= 0L) {
					clearCachedProgress();
					markDirty();

					if (file.isServerSide()) {
						NetworkManager.sendToPlayers(getOnlineMembers(), new ObjectStartedResetMessage(teamId, questId));
					}

					return true;
				}
			} else {
				if (started.put(questId, time.getTime()) == 0L) {
					clearCachedProgress();
					markDirty();

					if (file.isServerSide()) {
						NetworkManager.sendToPlayers(getOnlineMembers(), new ObjectStartedMessage(teamId, questId));
					}

					return true;
				}
			}

		}
		return false;
	}

	public Optional<Date> getCompletedTime(long questId) {
		long when = completed.get(questId);
		return when == 0L ? Optional.empty() : Optional.of(new Date(when));
	}

	public boolean setCompleted(long id, @Nullable Date time) {
		if (locked) {
			return false;
		}

		if (time == null) {
			if (completed.remove(id) >= 0L) {
				clearCachedProgress();
				markDirty();

				if (file.isServerSide()) {
					NetworkManager.sendToPlayers(getOnlineMembers(), new ObjectCompletedResetMessage(teamId, id));
				}

				return true;
			}
		} else {
			if (completed.put(id, time.getTime()) == 0L) {
				clearCachedProgress();
				markDirty();

				if (file.isServerSide()) {
					NetworkManager.sendToPlayers(getOnlineMembers(), new ObjectCompletedMessage(teamId, id));
				}

				return true;
			}
		}

		return false;
	}

	public Optional<Date> getRewardClaimTime(UUID player, Reward reward) {
		QuestKey key = QuestKey.forReward(player, reward);
		long t = claimedRewards.getLong(key);
		return t == 0L ? Optional.empty() : Optional.of(new Date(t));
	}

	public boolean areRewardsBlocked() {
		return rewardsBlocked;
	}

	public boolean isRewardBlocked(Reward reward) {
		return areRewardsBlocked()
				&& !reward.ignoreRewardBlocking()
				&& !reward.getQuest().ignoreRewardBlocking();
	}

	public boolean setRewardsBlocked(boolean rewardsBlocked) {
		if (rewardsBlocked != this.rewardsBlocked) {
			this.rewardsBlocked = rewardsBlocked;
			clearCachedProgress();
			markDirty();
			if (file.isServerSide()) {
				NetworkManager.sendToPlayers(getOnlineMembers(), new SyncRewardBlockingMessage(teamId, rewardsBlocked));
			}
			return true;
		}
		return false;
	}

	public boolean isRewardClaimed(UUID player, Reward reward) {
		return getRewardClaimTime(player, reward).isPresent();
	}

	public boolean hasUnclaimedRewards(UUID player, QuestObject object) {
		QuestKey key = QuestKey.create(player, object.id);
		byte b = unclaimedRewardsCache.getOrDefault(key, BOOL_UNKNOWN);
		if (b == BOOL_UNKNOWN) {
			b = object.hasUnclaimedRewardsRaw(this, player) ? BOOL_TRUE : BOOL_FALSE;
			unclaimedRewardsCache.put(key, b);
		}

		return b == BOOL_TRUE;
	}

	public boolean claimReward(UUID player, Reward reward, long date) {
		if (locked || isRewardBlocked(reward)) {
			return false;
		}

		QuestKey key = QuestKey.forReward(player, reward);

		if (!claimedRewards.containsKey(key)) {
			claimedRewards.put(key, date);
			clearCachedProgress();
			markDirty();

			if (file.isServerSide()) {
				NetworkManager.sendToPlayers(getOnlineMembers(), new ClaimRewardResponseMessage(teamId, player, reward.id));
			}

			reward.getQuest().checkRepeatable(this, player);

			return true;
		}

		return false;
	}

	public void deleteReward(Reward reward) {
		if (!locked && claimedRewards.object2LongEntrySet().removeIf(e -> e.getKey().getId() == reward.id)) {
			clearCachedProgress();
			markDirty();
		}
	}

	public boolean resetReward(UUID player, Reward reward) {
		if (!locked && claimedRewards.removeLong(QuestKey.forReward(player, reward)) != 0L) {
			clearCachedProgress();
			markDirty();

			if (file.isServerSide()) {
				NetworkManager.sendToPlayers(getOnlineMembers(), new ResetRewardMessage(teamId, player, reward.id));
			}

			return true;
		}

		return false;
	}

	public void clearCachedProgress() {
		areDependenciesCompleteCache.clear();
		areDependenciesVisibleCache.clear();
		unclaimedRewardsCache.clear();
		exclusionCache.clear();
	}

	public SNBTCompoundTag serializeNBT() {
		SNBTCompoundTag nbt = new SNBTCompoundTag();
		nbt.putInt("version", VERSION);
		nbt.putString("uuid", UndashedUuid.toString(teamId));
		nbt.putString("name", name);
		nbt.putBoolean("lock", locked);
		nbt.putBoolean("rewards_blocked", rewardsBlocked);

		SNBTCompoundTag taskProgressNBT = new SNBTCompoundTag();
		for (Long2LongMap.Entry entry : taskProgress.long2LongEntrySet()) {
			if (entry.getLongValue() <= Integer.MAX_VALUE) {
				taskProgressNBT.putInt(QuestObjectBase.getCodeString(entry.getLongKey()), (int) entry.getLongValue());
			} else {
				taskProgressNBT.putLong(QuestObjectBase.getCodeString(entry.getLongKey()), entry.getLongValue());
			}
		}
		nbt.put("task_progress", taskProgressNBT);

		SNBTCompoundTag startedNBT = new SNBTCompoundTag();
		for (Long2LongMap.Entry entry : started.long2LongEntrySet().stream().sorted(LONG2LONG_COMPARATOR).toList()) {
			startedNBT.putLong(QuestObjectBase.getCodeString(entry.getLongKey()), entry.getLongValue());
		}
		nbt.put("started", startedNBT);

		SNBTCompoundTag completedNBT = new SNBTCompoundTag();
		for (Long2LongMap.Entry entry : completed.long2LongEntrySet().stream().sorted(LONG2LONG_COMPARATOR).toList()) {
			completedNBT.putLong(QuestObjectBase.getCodeString(entry.getLongKey()), entry.getLongValue());
		}
		nbt.put("completed", completedNBT);

		SNBTCompoundTag claimedRewardsNBT = new SNBTCompoundTag();
		for (Object2LongMap.Entry<QuestKey> entry : claimedRewards.object2LongEntrySet().stream().sorted(OBJECT2LONG_COMPARATOR).toList()) {
			claimedRewardsNBT.putLong(entry.getKey().toString(), entry.getLongValue());
		}
		nbt.put("claimed_rewards", claimedRewardsNBT);

		CompoundTag ppdTag = new CompoundTag();
		perPlayerData.forEach((id, ppd) -> {
			if (!ppd.hasDefaultValues()) {
				ppdTag.put(UndashedUuid.toString(id), ppd.writeNBT());
			}
		});
		nbt.put("player_data", ppdTag);

		return nbt;
	}

	public void deserializeNBT(SNBTCompoundTag nbt) {
		int fileVersion = nbt.getInt("version");

		if (fileVersion != VERSION) {
			markDirty();
		}

		name = nbt.getString("name");
		locked = nbt.getBoolean("lock");
		rewardsBlocked = nbt.getBoolean("rewards_blocked");

		taskProgress.clear();
		claimedRewards.clear();
		perPlayerData.clear();

		CompoundTag claimedRewardsNBT = nbt.getCompound("claimed_rewards");
		for (String s : claimedRewardsNBT.getAllKeys()) {
			claimedRewards.put(QuestKey.fromString(s), claimedRewardsNBT.getLong(s));
		}

		CompoundTag taskProgressNBT = nbt.getCompound("task_progress");
		for (String s : taskProgressNBT.getAllKeys()) {
			taskProgress.put(file.getID(s), taskProgressNBT.getLong(s));
		}

		CompoundTag startedNBT = nbt.getCompound("started");
		for (String s : startedNBT.getAllKeys()) {
			started.put(file.getID(s), startedNBT.getLong(s));
		}

		CompoundTag completedNBT = nbt.getCompound("completed");
		for (String s : completedNBT.getAllKeys()) {
			completed.put(file.getID(s), completedNBT.getLong(s));
		}

		CompoundTag ppdTag = nbt.getCompound("player_data");
		for (String key : ppdTag.getAllKeys()) {
			try {
				UUID id = UndashedUuid.fromString(key);
				perPlayerData.put(id, PerPlayerData.fromNBT(ppdTag.getCompound(key), file));
			} catch (IllegalArgumentException e) {
				FTBQuests.LOGGER.error("ignoring invalid player ID {} while loading per-player data for team {}", key, teamId);
			}
		}
	}

	private void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(name, Short.MAX_VALUE);
		buffer.writeVarInt(taskProgress.size());

		for (Long2LongMap.Entry entry : taskProgress.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(entry.getLongValue());
		}

		long now = System.currentTimeMillis();

		buffer.writeVarInt(started.size());

		for (Long2LongOpenHashMap.Entry entry : started.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(now - entry.getLongValue());
		}

		buffer.writeVarInt(completed.size());

		for (Long2LongOpenHashMap.Entry entry : completed.long2LongEntrySet()) {
			buffer.writeLong(entry.getLongKey());
			buffer.writeVarLong(now - entry.getLongValue());
		}

		buffer.writeBoolean(locked);
		buffer.writeBoolean(rewardsBlocked);

		buffer.writeVarInt(claimedRewards.size());
		for (Object2LongMap.Entry<QuestKey> entry : claimedRewards.object2LongEntrySet()) {
			entry.getKey().toNetwork(buffer);
			buffer.writeVarLong(now - entry.getLongValue());
		}

		buffer.writeVarInt(perPlayerData.size());
		perPlayerData.forEach((id, ppd) -> {
			buffer.writeUUID(id);
			ppd.writeNet(buffer);
		});

	}

	private void readNetData(FriendlyByteBuf buffer) {
		name = buffer.readUtf(Short.MAX_VALUE);

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

		locked = buffer.readBoolean();
		rewardsBlocked = buffer.readBoolean();

		claimedRewards.clear();
		perPlayerData.clear();

		int claimedRewardCount = buffer.readVarInt();
		for (int i = 0; i < claimedRewardCount; i++) {
			QuestKey key = QuestKey.fromNetwork(buffer);
			claimedRewards.put(key, now - buffer.readVarLong());
		}

		int ppdCount = buffer.readVarInt();
		for (int i = 0; i < ppdCount; i++) {
			UUID id = buffer.readUUID();
			perPlayerData.put(id, PerPlayerData.fromNet(buffer));
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

	private boolean checkDepsCached(Quest quest, Long2ByteMap cache, ToBooleanBiFunction<Quest,TeamData> checker) {
		if (!quest.hasDependencies()) {
			return true;
		}

		byte res = cache.getOrDefault(quest.id, BOOL_UNKNOWN);
		if (res == BOOL_UNKNOWN) {
			res = checker.applyAsBoolean(quest, this) ? BOOL_TRUE : BOOL_FALSE;
			cache.put(quest.id, res);
		}

		return res == BOOL_TRUE;
	}

	public boolean areDependenciesComplete(Quest quest) {
		return checkDepsCached(quest, areDependenciesCompleteCache, Quest::areDependenciesComplete);
	}

	public boolean areDependenciesVisible(Quest quest) {
		return checkDepsCached(quest, areDependenciesVisibleCache, Quest::areDependenciesVisible);
	}

	public boolean canStartTasks(Quest quest) {
		return !isExcludedByOtherQuestline(quest) &&
				(quest.getProgressionMode() == ProgressionMode.FLEXIBLE || areDependenciesComplete(quest));
	}

	public void claimReward(ServerPlayer player, Reward reward, boolean notify) {
		if (claimReward(player.getUUID(), reward, System.currentTimeMillis())) {
			reward.claim(player, notify);
		}
	}

	public Collection<ServerPlayer> getOnlineMembers() {
		return FTBTeamsAPI.api().getManager().getTeamByID(teamId)
				.map(Team::getOnlineMembers)
				.orElse(List.of());
	}

	public void checkAutoCompletion(Quest quest) {
		if (quest.getRewards().isEmpty() || !isCompleted(quest)) {
			return;
		}

		Collection<ServerPlayer> online = null;

		for (Reward reward : quest.getRewards()) {
			RewardAutoClaim auto = reward.getAutoClaimType();

			if (auto != RewardAutoClaim.DISABLED) {
				if (reward.isTeamReward() && ServerQuestFile.INSTANCE.getCurrentPlayer() != null) {
					// only the submitting player gets the reward if it's a team reward
					// (assuming it's possible to determine who the submitting player is)
					claimReward(ServerQuestFile.INSTANCE.getCurrentPlayer(), reward, auto == RewardAutoClaim.ENABLED);
				} else {
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
	}

	public RewardClaimType getClaimType(UUID player, Reward reward) {
		boolean r = isRewardClaimed(player, reward);

		if (r) {
			return RewardClaimType.CLAIMED;
		} else if (isCompleted(reward.getQuest())) {
			return RewardClaimType.CAN_CLAIM;
		}

		return RewardClaimType.CANT_CLAIM;
	}

	public void resetProgress(Task task) {
		if (taskProgress.remove(task.id) > 0L) {
			markDirty();
		}
	}

	public final void setProgress(Task task, long progress) {
		if (locked) {
			return;
		}

		long maxProgress = task.getMaxProgress();
		progress = Math.max(0L, Math.min(progress, maxProgress));
		long prevProgress = getProgress(task);

		if (prevProgress != progress || progress == 0L && isStarted(task)) {
			if (progress == 0L) {
				taskProgress.remove(task.id);
				started.remove(task.id);
				completed.remove(task.id);
			} else {
				taskProgress.put(task.id, progress);
			}

			clearCachedProgress();

			if (file.isServerSide()) {
				Date now = new Date();
				Collection<ServerPlayer> onlineMembers = getOnlineMembers();

				NetworkManager.sendToPlayers(getOnlineMembers(), new UpdateTaskProgressMessage(this.getTeamId(), task.id, progress));

				if (prevProgress == 0L) {
					task.onStarted(new QuestProgressEventData<>(now, this, task, onlineMembers, Collections.emptyList()));
				}

				// if we're in flexible progress mode, we can get here without dependencies being complete yet
				if (progress >= maxProgress && areDependenciesComplete(task.getQuest())) {
					markTaskCompleted(task);
				}
			}

			markDirty();
		}
	}

	public void markTaskCompleted(Task task) {
		Collection<ServerPlayer> onlineMembers = getOnlineMembers();
		Collection<ServerPlayer> notifiedPlayers;

		if (!task.getQuest().getChapter().isAlwaysInvisible() && QuestObjectBase.shouldSendNotifications()) {
			notifiedPlayers = onlineMembers;
		} else {
			notifiedPlayers = List.of();
		}

		task.onCompleted(new QuestProgressEventData<>(new Date(), this, task, onlineMembers, notifiedPlayers));

		for (ServerPlayer player : onlineMembers) {
			FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, task.id);
		}

		if (isCompleted(task.getQuest())) {
			perPlayerData.values().forEach(data -> data.pinnedQuests.remove(task.getQuest().id));
			markDirty();
			NetworkManager.sendToPlayers(getOnlineMembers(), new TogglePinnedResponseMessage(task.getQuest().id, false));
		}
	}

	public final void addProgress(Task task, long progress) {
		setProgress(task, getProgress(task) + progress);
	}

	public boolean isLocked() {
		return locked;
	}

	public boolean setLocked(boolean newLocked) {
		if (locked != newLocked) {
			locked = newLocked;
			clearCachedProgress();
			markDirty();

			if (file.isServerSide()) {
				NetworkManager.sendToPlayers(getOnlineMembers(), new SyncLockMessage(teamId, locked));
			}

			return true;
		}

		return false;
	}

	public void mergeData(TeamData from) {
		// used when joining an existing party team
		from.taskProgress.forEach((id, data) -> taskProgress.mergeLong(id, data, Long::max));

		from.started.forEach((id, data) -> started.mergeLong(id, data, (oldVal, newVal) -> oldVal));
		from.completed.forEach((id, data) -> completed.mergeLong(id, data, (oldVal, newVal) -> oldVal));
		from.claimedRewards.forEach((id, data) -> claimedRewards.mergeLong(id, data, (oldVal, newVal) -> oldVal));

		from.perPlayerData.forEach((id, data) -> perPlayerData.merge(id, data, (oldVal, newVal) -> oldVal));
	}

	public void mergeClaimedRewards(TeamData from) {
		// used when leaving a party team - copy claimed-reward data from to player team
		from.claimedRewards.forEach((questKey, data) -> {
			if (questKey.uuid().equals(teamId)) {
				claimedRewards.put(questKey, data.longValue());
			}
		});
	}

	public void copyData(TeamData from) {
		locked = from.locked;
		taskProgress.putAll(from.taskProgress);
		claimedRewards.putAll(from.claimedRewards);
		started.putAll(from.started);
		completed.putAll(from.completed);
		perPlayerData.putAll(from.perPlayerData);
		rewardsBlocked = from.rewardsBlocked;
	}

	/**
	 * Get the per-player data for the given player, creating it if it doesn't exist yet - but only if the player is a
	 * member of this team!
	 *
	 * @param player the player
	 * @return the player's per-player data, or {@code Optional.empty()} if the player isn't in this team
	 */
	private Optional<PerPlayerData> getOrCreatePlayerData(Player player) {
		if (!perPlayerData.containsKey(player.getUUID()) && file.isPlayerOnTeam(player, this)) {
			perPlayerData.put(player.getUUID(), new PerPlayerData());
		}
		return Optional.ofNullable(perPlayerData.get(player.getUUID()));
	}

	public boolean getCanEdit(Player player) {
		return getOrCreatePlayerData(player).map(d -> d.canEdit).orElse(false);
	}

	public boolean setCanEdit(Player player, boolean newCanEdit) {
		return getOrCreatePlayerData(player).map(playerData -> {
			if (playerData.canEdit != newCanEdit) {
				playerData.canEdit = newCanEdit;
				clearCachedProgress();
				markDirty();
				if (file.isServerSide() && player instanceof ServerPlayer sp) {
					NetworkManager.sendToPlayer(sp, new SyncEditingModeMessage(teamId, newCanEdit));
				}
				return true;
			}
			return false;
		}).orElse(false);
	}

	public boolean isQuestPinned(Player player, long id) {
		return getOrCreatePlayerData(player).map(playerData -> playerData.pinnedQuests.contains(id)).orElse(false);
	}

	public void setQuestPinned(Player player, long id, boolean pinned) {
		getOrCreatePlayerData(player).ifPresent(playerData -> {
			if (pinned ? playerData.pinnedQuests.add(id) : playerData.pinnedQuests.remove(id)) {
				markDirty();
			}
		});
	}

	public void setChapterPinned(Player player, boolean pinned) {
		getOrCreatePlayerData(player).ifPresent(playerData -> {
			if (playerData.chapterPinned != pinned) {
				playerData.chapterPinned = pinned;
				markDirty();
			}
		});
	}

	public boolean isChapterPinned(Player player) {
		return getOrCreatePlayerData(player).map(playerData -> playerData.chapterPinned).orElse(false);
	}

	public LongSet getPinnedQuestIds(Player player) {
		return getOrCreatePlayerData(player).map(playerData -> playerData.pinnedQuests).orElse(LongSet.of());
	}

	public boolean isExcludedByOtherQuestline(QuestObject qo) {
		return qo instanceof Excludable e && exclusionCache.computeIfAbsent(e.getId(), k -> e.isQuestObjectExcluded(this));
	}

	private static class PerPlayerData {
		private boolean canEdit;
		private boolean autoPin;
		private boolean chapterPinned;
		private final LongSet pinnedQuests;

		PerPlayerData() {
			canEdit = autoPin = chapterPinned = false;
			pinnedQuests = new LongOpenHashSet();
		}

		PerPlayerData(boolean canEdit, boolean autoPin, boolean chapterPinned, LongSet pinnedQuests) {
			this.canEdit = canEdit;
			this.autoPin = autoPin;
			this.chapterPinned = chapterPinned;
			this.pinnedQuests = pinnedQuests;
		}

		public boolean hasDefaultValues() {
			return !canEdit && !autoPin && !chapterPinned && pinnedQuests.isEmpty();
		}

		public static PerPlayerData fromNBT(CompoundTag nbt, BaseQuestFile file) {
			boolean canEdit = nbt.getBoolean("can_edit");
			boolean autoPin = nbt.getBoolean("auto_pin");
			boolean chapterPinned = nbt.getBoolean("chapter_pinned");
			LongSet pq = nbt.getList("pinned_quests", Tag.TAG_STRING).stream()
					.map(tag -> file.getID(tag.getAsString()))
					.collect(Collectors.toCollection(LongOpenHashSet::new));
			return new PerPlayerData(canEdit, autoPin, chapterPinned, pq);
		}

		public static PerPlayerData fromNet(FriendlyByteBuf buffer) {
			PerPlayerData ppd = new PerPlayerData();

			ppd.canEdit = buffer.readBoolean();
			ppd.autoPin = buffer.readBoolean();
			ppd.chapterPinned = buffer.readBoolean();
			int pinnedCount = buffer.readVarInt();
			for (int i = 0; i < pinnedCount; i++) {
				ppd.pinnedQuests.add(buffer.readLong());
			}

			return ppd;
		}

		public CompoundTag writeNBT() {
			CompoundTag nbt = new CompoundTag();

			if (canEdit) nbt.putBoolean("can_edit", true);
			if (autoPin) nbt.putBoolean("auto_pin", true);
			if (chapterPinned) nbt.putBoolean("chapter_pinned", true);

			if (!pinnedQuests.isEmpty()) {
				long[] pinnedQuestsArray = pinnedQuests.toLongArray();
				Arrays.sort(pinnedQuestsArray);
				ListTag pinnedQuestsNBT = new ListTag();
				for (long l : pinnedQuestsArray) {
					pinnedQuestsNBT.add(StringTag.valueOf(QuestObjectBase.getCodeString(l)));
				}
				nbt.put("pinned_quests", pinnedQuestsNBT);
			}

			return nbt;
		}

		public void writeNet(FriendlyByteBuf buffer) {
			buffer.writeBoolean(canEdit);
			buffer.writeBoolean(autoPin);
			buffer.writeBoolean(chapterPinned);

			buffer.writeVarInt(pinnedQuests.size());
			for (long reward : pinnedQuests) {
				buffer.writeLong(reward);
			}
		}
	}
}
