package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UndashedUuid;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.net.*;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbquests.util.PlayerInventorySummary;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ServerQuestFile extends BaseQuestFile {
	public static final LevelResource FTBQUESTS_DATA = new LevelResource("ftbquests");

	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private Path folder;
	private ServerPlayer currentPlayer = null;

	public ServerQuestFile(MinecraftServer s) {
		server = s;
		shouldSave = false;
		isLoading = false;

		int taskTypeId = 0;

		for (TaskType type : TaskTypes.TYPES.values()) {
			type.internalId = ++taskTypeId;
			taskTypeIds.put(type.internalId, type);
		}

		int rewardTypeId = 0;

		for (RewardType type : RewardTypes.TYPES.values()) {
			type.intId = ++rewardTypeId;
			rewardTypeIds.put(type.intId, type);
		}
	}

	public void load() {
		load(true, true);
	}

	public void load(boolean quests, boolean progression) {
		folder = Platform.getConfigFolder().resolve("ftbquests/quests");

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
						SNBTCompoundTag nbt = SNBT.read(path1);
						if (nbt != null) {
							try {
								UUID uuid = UndashedUuid.fromString(nbt.getString("uuid"));
								TeamData data = new TeamData(uuid, this);
								addData(data, true);
								data.deserializeNBT(nbt);
							} catch (Exception ex) {
								FTBQuests.LOGGER.error("can't parse progression data for {}: {}", path1, ex.getMessage());
							}
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
			getTranslationManager().removeAllTranslations(object);
			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			markDirty();
			object.getPath().ifPresent(path -> FileUtils.delete(getFolder().resolve(path).toFile()));
		}

		NetworkHelper.sendToAll(server, new DeleteObjectResponseMessage(id));
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

		getTranslationManager().saveToNBT(getFolder().resolve("lang"), false);

		getAllTeamData().forEach(TeamData::saveIfChanged);
	}

	public void unload() {
		saveNow();
		deleteChildren();
		deleteSelf();
	}

	public ServerPlayer getCurrentPlayer() {
		return currentPlayer;
	}

	public void withPlayerContext(ServerPlayer player, Runnable toDo) {
		currentPlayer = player;
		try {
			toDo.run();
		} finally {
			currentPlayer = null;
		}
	}

	public void playerLoggedIn(PlayerLoggedInAfterTeamEvent event) {
		ServerPlayer player = event.getPlayer();
		TeamData data = getOrCreateTeamData(event.getTeam());

		// Sync the quest book data
		// - client will respond to this with a RequestTeamData message
		// - server will only then send a SyncTeamData message to the client
		NetworkManager.sendToPlayer(player, new SyncQuestsMessage(this));

		NetworkManager.sendToPlayer(player, new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(player, false)));

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
						quest.onCompleted(new QuestProgressEventData<>(now, data, quest, onlineMembers, pList));
					}

					data.checkAutoCompletion(quest);

					if (data.canStartTasks(quest)) {
						quest.getTasks().stream().filter(Task::checkOnLogin).forEach(task -> task.submitTask(data, player));
					}
				});
			});
		}
	}

	public void teamCreated(TeamCreatedEvent event) {
		UUID id = event.getTeam().getId();

		TeamData data = teamDataMap.computeIfAbsent(id, k -> {
			TeamData newTeamData = new TeamData(id, this);
			newTeamData.markDirty();
			return newTeamData;
		});

		data.setName(event.getTeam().getShortName());

		addData(data, false);

		if (event.getTeam() instanceof PartyTeam) {
			FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(event.getCreator().getUUID()).ifPresent(playerTeam -> {
				TeamData oldTeamData = getOrCreateTeamData(playerTeam);
				data.copyData(oldTeamData);
			});
		}

		NetworkHelper.sendToAll(server, new CreateOtherTeamDataMessage(TeamDataUpdate.forTeamData(data)));
	}

	public void playerChangedTeam(PlayerChangedTeamEvent event) {
		event.getPreviousTeam().ifPresent(prevTeam -> {
			Team curTeam = event.getTeam();
			TeamData oldTeamData = getOrCreateTeamData(prevTeam);
			TeamData newTeamData = getOrCreateTeamData(curTeam);

			if (prevTeam.isPlayerTeam() && curTeam.isPartyTeam() && !curTeam.getOwner().equals(event.getPlayerId())) {
				// player is joining an existing party team; merge all of their progress data into the party
				newTeamData.mergeData(oldTeamData);
				// also check if the party team has any outstanding auto-claim rewards that the player can claim
				withPlayerContext(event.getPlayer(), () -> forAllQuests(newTeamData::checkAutoCompletion));
			} else if (prevTeam.isPartyTeam() && curTeam.isPlayerTeam()) {
				// player is leaving an existing party team; they get their old progress back
				// EXCEPT any rewards they've already claimed stay claimed! no claiming the reward again
				newTeamData.mergeClaimedRewards(oldTeamData);
			}

			NetworkHelper.sendToAll(server, new TeamDataChangedMessage(TeamDataUpdate.forTeamData(oldTeamData), TeamDataUpdate.forTeamData(newTeamData)));
			NetworkManager.sendToPlayers(curTeam.getOnlineMembers(), new SyncTeamDataMessage(newTeamData));
		});
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
			NetworkHelper.sendToAll(server, new MoveChapterGroupResponseMessage(id, movingUp));
			return true;
		}
		return false;
	}

	@Override
	public String getLocale() {
		return getFallbackLocale();
	}
}
