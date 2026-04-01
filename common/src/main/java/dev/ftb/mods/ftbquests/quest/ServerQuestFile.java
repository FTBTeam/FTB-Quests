package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UndashedUuid;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.Env;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.progress.ProgressEventData;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.*;
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
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
	@Nullable
	private ServerPlayer currentPlayer = null;

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
		return INSTANCE != null && !INSTANCE.invalid;
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
				readDataFull(folder, server.registryAccess());
				isLoading = false;
			}
		}

		if (progression) {
			Path path = server.getWorldPath(FTBQUESTS_DATA);

			if (Files.exists(path)) {
				try (Stream<Path> s = Files.list(path)) {
					s.filter(p -> p.getFileName().toString().contains("-") && p.getFileName().toString().endsWith(".snbt")).forEach(path1 -> {
						try {
							var json = Json5Util.tryRead(path1);
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
	public void deleteObject(long id) {
		QuestObjectBase object = getBase(id);

		if (object != null) {
			object.getPath().ifPresent(path -> {
				try {
					Files.delete(getFolder().resolve(path));
				} catch (IOException e) {
					FTBQuests.LOGGER.error("can't delete {}: {}", getFolder().resolve(path), e.getMessage());
				}
			});
			getTranslationManager().removeAllTranslations(object);
			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			markDirty();
		}

		Server2PlayNetworking.sendToAllPlayers(server, new DeleteObjectResponseMessage(id));
	}

	@Override
	public void markDirty() {
		shouldSave = true;
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
		deleteChildren();
		deleteSelf();
	}

	@Nullable
	public ServerPlayer getCurrentPlayer() {
		return currentPlayer;
	}

	public void withPlayerContext(@Nullable ServerPlayer player, Runnable toDo) {
		currentPlayer = player;
		try {
			toDo.run();
		} finally {
			currentPlayer = null;
		}
	}

	public void playerLoggedIn(TeamPlayerLoggedInEvent.Data event) {
		ServerPlayer player = event.player();
		TeamData data = getOrCreateTeamData(event.team());

		if (data.getName().isEmpty()) {
			data.setName(player.getPlainTextName());
			Server2PlayNetworking.send(player, new UpdateTeamDataMessage(data.getTeamId(), data.getName()));
		}

		// Sync the quest book data
		// - client will respond to this with a RequestTeamData message
		// - server will only then send a SyncTeamData message to the client
		Server2PlayNetworking.send(player, new SyncQuestsMessage(this));

		Server2PlayNetworking.send(player, new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(player, false)));

		getTranslationManager().sendTranslationsToPlayer(player);

		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

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

		TeamData data = teamDataMap.computeIfAbsent(id, k -> {
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
				withPlayerContext(event.player(), () -> forAllQuests(newTeamData::checkAutoCompletion));
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
			Server2PlayNetworking.sendToAllPlayers(server, new MoveChapterGroupResponseMessage(id, movingUp));
			return true;
		}
		return false;
	}

	@Override
	public String getLocale() {
		return getFallbackLocale();
	}
}
