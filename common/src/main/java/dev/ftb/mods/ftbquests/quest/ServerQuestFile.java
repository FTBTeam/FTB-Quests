package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UUIDTypeAdapter;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
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
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
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
		folder = Platform.getConfigFolder().resolve("ftbquests/quests");

		if (Files.exists(folder)) {
			FTBQuests.LOGGER.info("Loading quests from " + folder);
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}

		Path path = server.getWorldPath(FTBQUESTS_DATA);

		if (Files.exists(path)) {
			try (Stream<Path> s = Files.list(path)) {
				s.filter(p -> p.getFileName().toString().contains("-") && p.getFileName().toString().endsWith(".snbt")).forEach(path1 -> {
					SNBTCompoundTag nbt = SNBT.read(path1);

					if (nbt != null) {
						try {
							UUID uuid = UUIDTypeAdapter.fromString(nbt.getString("uuid"));
							TeamData data = new TeamData(uuid, this);
							addData(data, true);
							data.deserializeNBT(nbt);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public Env getSide() {
		return Env.SERVER;
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
			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			markDirty();
			object.getPath().ifPresent(path -> FileUtils.delete(getFolder().resolve(path).toFile()));
		}

		new DeleteObjectResponseMessage(id).sendToAll(server);
	}

	@Override
	public void markDirty() {
		shouldSave = true;
	}

	public void saveNow() {
		if (getFolder() != null) {
			if (shouldSave) {
				writeDataFull(getFolder());
				shouldSave = false;
			}

			getAllTeamData().forEach(TeamData::saveIfChanged);
		}
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
		new SyncQuestsMessage(this).sendTo(player);

		new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(player, false)).sendTo(player);

		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		if (!data.isLocked()) {
			withPlayerContext(player, () -> forAllQuests(quest -> {
				if (!data.isCompleted(quest) && quest.isCompletedRaw(data)) {
					// Handles possible situation where quest book has been modified to remove a task from a quest
					// It can leave a player having completed all the other tasks, but unable to complete the quest
					//   since quests are normally marked completed when the last task in that quest is completed
					// https://github.com/FTBBeta/Beta-Testing-Issues/issues/755
					quest.onCompleted(new QuestProgressEventData<>(new Date(), data, quest, data.getOnlineMembers(), Collections.singletonList(player)));
				}

				data.checkAutoCompletion(quest);

				if (data.canStartTasks(quest)) {
					quest.getTasks().stream().filter(Task::checkOnLogin).forEach(task -> task.submitTask(data, player));
				}
			}));
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

		TeamDataUpdate self = new TeamDataUpdate(data);

		new CreateOtherTeamDataMessage(self).sendToAll(server);
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

			new TeamDataChangedMessage(new TeamDataUpdate(oldTeamData), new TeamDataUpdate(newTeamData)).sendToAll(server);
			new SyncTeamDataMessage(newTeamData, true).sendTo(curTeam.getOnlineMembers());
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
			new MoveChapterGroupResponseMessage(id, movingUp).sendToAll(server);
			return true;
		}
		return false;
	}
}
