package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.Env;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.event.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.history.HistoryStack;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.PlayerInventorySummary;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamPlayerLoggedInEvent;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ServerQuestFile extends BaseQuestFile {
	public static final LevelResource FTBQUESTS_DATA = new LevelResource("ftbquests");

	@Nullable
	private static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private final Path folder;
	private final Deque<ServerPlayer> playerContextStack = new ArrayDeque<>();
	private final HistoryStack historyStack;

	public static void startup(MinecraftServer server) {
		INSTANCE = new ServerQuestFile(server);
	}

	public static void shutdown() {
		ServerQuestFile sqf = getInstance();
		sqf.saveNow();
		sqf.unload();
		INSTANCE = null;
	}

	public static boolean exists() {
		return INSTANCE != null && INSTANCE.isValid();
	}

	public static ServerQuestFile getInstance() {
		return Objects.requireNonNull(INSTANCE);
	}

	public static void ifExists(Consumer<ServerQuestFile> consumer) {
		if (INSTANCE != null && INSTANCE.isValid()) {
			consumer.accept(INSTANCE);
		}
	}

	public ServerQuestFile(MinecraftServer s) {
		server = s;
		shouldSave = false;
		isLoading = false;
		historyStack = new HistoryStack();

		folder = Platform.get().paths().configPath().resolve("ftbquests/quests");

		int taskTypeId = 0;

		for (TaskType type : TaskTypes.TYPES.values()) {
			type.internalId = ++taskTypeId;
			taskTypeIds.put(type.internalId, type);
		}

		int rewardTypeId = 0;

		for (RewardType type : RewardTypes.TYPES.values()) {
			type.internalId = ++rewardTypeId;
			rewardTypeIds.put(type.internalId, type);
		}
	}

	public void load(boolean quests, boolean progression) {
		if (quests) {
			if (Files.exists(folder)) {
				FTBQuests.LOGGER.info("Loading quests from {}", folder);
				isLoading = true;
				try {
					readDataFull(folder, server.registryAccess());
				} catch (Exception ex) {
					FTBQuests.LOGGER.error("failed to load quest data from {}: {}", folder, ex.getMessage(), ex);
				} finally {
					isLoading = false;
				}
			}
		}

		if (progression) {
			Path path = server.getWorldPath(FTBQUESTS_DATA);

			if (Files.exists(path)) {
				try (Stream<Path> s = Files.list(path)) {
					s.filter(p -> p.getFileName().toString().contains("-") && p.getFileName().toString().endsWith(Json5Util.FILE_EXT)).forEach(path1 -> {
						try {
							var json = Json5Util.load(path1);
							UUID uuid = UndashedUuid.fromString(Json5Util.getString(json, "uuid").orElseThrow());
							TeamData data = new TeamData(uuid, true);
							addData(data, true);
							data.deserializeJson(json);
						} catch (Exception ex) {
							FTBQuests.LOGGER.error("can't parse progression data for {}: {}", path1, ex.getMessage());
						}
					});
				} catch (Exception ex) {
					FTBQuests.LOGGER.error("can't read directory {}: {}", path, ex.getMessage());
				}
			}
		}
	}

	@Override
	public Env getSide() {
		return Env.SERVER;
	}

	@Override
	public HolderLookup.Provider holderLookup() {
		return server.registryAccess();
	}

	@Override
	public boolean isLoading() {
		return isLoading;
	}

	@Override
	public Path getFolder() {
		return folder;
	}

	@Override
	public boolean deleteObjects(List<Long> ids) {
		List<Long> deletedIds = new ArrayList<>();

		List<QuestObjectBase> toDelete = new ArrayList<>();
		for (long id : ids) {
			var qob = getBase(id);
			if (qob == null || qob instanceof BaseQuestFile) {
				return false;
			}
			toDelete.add(qob);
		}

		for (var qob : toDelete) {
			getTranslationManager().removeAllTranslations(qob);
			qob.deleteSelf();
			qob.getPath().ifPresent(path -> {
				try {
					FileUtils.delete(getFolder().resolve(path).toFile());
				} catch (IOException e) {
					FTBQuests.LOGGER.error("can't delete {}: {}", path, e.getMessage());
				}
			});
			deletedIds.add(id);
		}

		if (deletedIds.size() == toDelete.size()) {
			markDirty();
			return true;
		}

		return false;
	}

	@Override
	public void markDirty() {
		shouldSave = true;
	}

	public HistoryStack getHistoryStack() {
		return historyStack;
	}

	public void saveNow() {
		if (shouldSave) {
			writeDataFull(getFolder(), server.registryAccess());
			shouldSave = false;
		}

		getTranslationManager().saveToFile(this, getFolder().resolve("lang"), false);

		getAllTeamData().forEach(TeamData::saveIfChanged);
	}

	public void unload() {
		saveNow();
		deleteSelf();
	}

	@Nullable
	public ServerPlayer getCurrentPlayer() {
		return playerContextStack.peek();
	}

	public void withPlayerContext(ServerPlayer player, Runnable toDo) {
		playerContextStack.push(player);
		try {
			toDo.run();
		} finally {
			playerContextStack.pop();
		}
	}

	public void playerLoggedIn(TeamPlayerLoggedInEvent.Data event) {
		ServerPlayer player = event.player();

		// Sync the quest book data
		// - client will respond to this with a RequestTeamData message
		// - server will only then send a SyncTeamData message to the client
		Server2PlayNetworking.send(player, new SyncQuestsMessage(this));

		Server2PlayNetworking.send(player, new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(player)));

		getTranslationManager().sendTranslationsToPlayer(player);

		Server2PlayNetworking.send(player, historyStack.createInitialDescPacket(this));

		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		TeamData data = getOrCreateTeamData(event.team());

		if (data.getName().isEmpty()) {
			data.setName(player.getPlainTextName());
			Server2PlayNetworking.send(player, new UpdateTeamDataMessage(data.getTeamId(), data.getName()));
		}

		if (data.isPlayerInEditMode(player) && !PermissionsHelper.canPlayerEdit(player)) {
			// could happen if player was deop'd or lost the "ftbquests.editor" permission node
			data.setPlayerEditMode(player, false);
		}

		checkQuestBookOnLogin(data, player);
	}

	private void checkQuestBookOnLogin(TeamData data, ServerPlayer player) {
		if (!data.isLocked()) {
			withPlayerContext(player, () -> {
				var onlineMembers = data.getOnlineMembers();
				var pList = List.of(player);
				Date now = new Date();
				PlayerInventorySummary.build(player);
				forAllQuests(quest -> {
					if (!data.isCompleted(quest) && quest.getProgressionMode() == ProgressionMode.FLEXIBLE && data.areDependenciesComplete(quest)) {
						for (Task task : quest.getTasks()) {
							if (!data.isCompleted(task) && data.getProgress(task) >= task.getMaxProgress()) {
								data.markTaskCompleted(task);
							}
						}
					}

					if (!data.isCompleted(quest) && quest.isCompletedRaw(data)) {
						// Handles possible situation where quest book has been modified to remove a task from a quest
						// It can leave a player having completed all the other tasks, but unable to complete the quest
						//   since quests are normally marked completed when the last task in that quest is completed
						// https://github.com/FTBBeta/Beta-Testing-Issues/issues/755
						quest.onCompleted(new ProgressEventData<>(now, data, quest, onlineMembers, pList));
					}

					data.checkAutoCompletion(quest);

					if (data.canStartTasks(quest)) {
						quest.getTasks().stream().filter(Task::checkOnLogin).forEach(task -> task.submitTask(data, player));
					}
				});
			});
		}
	}

	public void teamCreated(TeamCreatedEvent.Data event) {
		UUID id = event.team().getId();

		TeamData data = teamDataMap.computeIfAbsent(id, _ -> {
			TeamData newTeamData = new TeamData(id, true);
			newTeamData.markDirty();
			return newTeamData;
		});

		data.setName(event.team().getShortName());

		addData(data, false);

		if (event.team() instanceof PartyTeam) {
			FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(event.creatorId()).ifPresent(playerTeam -> {
				TeamData oldTeamData = getOrCreateTeamData(playerTeam);
				data.copyData(oldTeamData);
			});
		}

		Server2PlayNetworking.sendToAllPlayers(server, new CreateOtherTeamDataMessage(TeamDataChangedMessage.TeamDataUpdate.forTeamData(data)));
	}

	public void playerChangedTeam(PlayerChangedTeamEvent.Data event) {
		Team prevTeam = event.previousTeam();
		if (prevTeam != null) {
			Team curTeam = event.team();
			TeamData oldTeamData = getOrCreateTeamData(prevTeam);
			TeamData newTeamData = getOrCreateTeamData(curTeam);

			if (prevTeam.isPlayerTeam() && curTeam.isPartyTeam() && !curTeam.getOwner().equals(event.playerId())) {
				// player is joining an existing party team; merge all of their progress data into the party
				newTeamData.mergeData(oldTeamData);
				// also check if the party team has any outstanding auto-claim rewards that the player can claim
				if (event.player() != null) {
					withPlayerContext(event.player(), () -> forAllQuests(newTeamData::checkAutoCompletion));
				}
			} else if (prevTeam.isPartyTeam() && curTeam.isPlayerTeam()) {
				// player is leaving an existing party team; they get their old progress back
				// EXCEPT any rewards they've already claimed stay claimed! no claiming the reward again
				newTeamData.mergeClaimedRewards(oldTeamData);
			}

			Server2PlayNetworking.sendToAllPlayers(server, new TeamDataChangedMessage(TeamDataChangedMessage.TeamDataUpdate.forTeamData(oldTeamData), TeamDataChangedMessage.TeamDataUpdate.forTeamData(newTeamData)));
			SyncTeamDataMessage msg = new SyncTeamDataMessage(newTeamData);
			curTeam.getOnlineMembers().forEach(p -> Server2PlayNetworking.send(p, msg));
		}
	}

	@Override
	public boolean isPlayerOnTeam(Player player, TeamData teamData) {
		return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID())
				.map(team -> team.getTeamId().equals(teamData.getTeamId()))
				.orElse(false);
	}

	@Override
	public boolean moveChapterGroup(long id, boolean movingUp) {
		if (super.moveChapterGroup(id, movingUp)) {
			markDirty();
			clearCachedData();
			return true;
		}
		return false;
	}

	@Override
	public String getLocale() {
		return getFallbackLocale();
	}
}
