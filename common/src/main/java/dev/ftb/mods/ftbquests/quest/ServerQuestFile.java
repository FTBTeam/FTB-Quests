package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UUIDTypeAdapter;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.CreateOtherTeamDataMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncQuestsMessage;
import dev.ftb.mods.ftbquests.net.SyncTeamDataMessage;
import dev.ftb.mods.ftbquests.net.TeamDataChangedMessage;
import dev.ftb.mods.ftbquests.net.TeamDataUpdate;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamCreatedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile {
	// TODO: validate
	public static final LevelResource FTBQUESTS_DATA = new LevelResource("ftbquests");

	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private Path folder;

	public ServerPlayer currentPlayer = null;

	public ServerQuestFile(MinecraftServer s) {
		server = s;
		shouldSave = false;
		isLoading = false;

		int taskTypeId = 0;

		for (TaskType type : TaskTypes.TYPES.values()) {
			type.intId = ++taskTypeId;
			taskTypeIds.put(type.intId, type);
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
			try {
				Files.list(path).filter(p -> p.getFileName().toString().contains("-")).forEach(path1 -> {
					SNBTCompoundTag nbt = SNBT.read(path1);

					if (nbt != null) {
						try {
							UUID uuid = UUIDTypeAdapter.fromString(nbt.getString("uuid"));
							TeamData data = new TeamData(uuid);
							data.file = this;
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
			String file = object.getPath();

			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			save();

			if (file != null) {
				FileUtils.delete(getFolder().resolve(file).toFile());
			}
		}

		new DeleteObjectResponseMessage(id).sendToAll(server);
	}

	@Override
	public void save() {
		shouldSave = true;
	}

	public void saveNow() {
		if (shouldSave) {
			writeDataFull(getFolder());
			shouldSave = false;
		}

		Path path = server.getWorldPath(FTBQUESTS_DATA);

		for (TeamData data : getAllData()) {
			if (data.shouldSave) {
				SNBT.write(path.resolve(data.uuid + ".snbt"), data.serializeNBT());
				data.shouldSave = false;
			}
		}
	}

	public void unload() {
		saveNow();
		deleteChildren();
		deleteSelf();
	}

	public void playerLoggedIn(PlayerLoggedInAfterTeamEvent event) {
		ServerPlayer player = event.getPlayer();
		TeamData data = getData(event.getTeam());

		new SyncQuestsMessage(this).sendTo(player);

		for (TeamData teamData : teamDataMap.values()) {
			new SyncTeamDataMessage(teamData, teamData == data).sendTo(player);
		}

		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		if (!data.isLocked()) {
			currentPlayer = player;

			for (ChapterGroup group : chapterGroups) {
				for (Chapter chapter : group.chapters) {
					for (Quest quest : chapter.quests) {
						data.checkAutoCompletion(quest);

						if (data.canStartTasks(quest)) {
							for (Task task : quest.tasks) {
								if (task.checkOnLogin()) {
									task.submitTask(data, player);
								}
							}
						}
					}
				}
			}

			currentPlayer = null;
		}
	}

	public void teamCreated(TeamCreatedEvent event) {
		UUID id = event.getTeam().getId();
		TeamData data = teamDataMap.get(id);

		if (data == null) {
			data = new TeamData(id);
			data.file = this;
			data.save();
		}

		String displayName = event.getTeam().getDisplayName();

		if (!data.name.equals(displayName)) {
			data.name = displayName;
			data.save();
		}

		addData(data, false);

		if (event.getTeam() instanceof PartyTeam) {
			PlayerTeam pt = event.getTeam().manager.getInternalPlayerTeam(event.getCreator().getUUID());
			TeamData oldTeamData = getData(pt);
			data.copyData(oldTeamData);
		}

		TeamDataUpdate self = new TeamDataUpdate(data);

		new CreateOtherTeamDataMessage(self).sendToAll(server);
	}

	public void playerChangedTeam(PlayerChangedTeamEvent event) {
		if (event.getPreviousTeam().isPresent()) {
			TeamData oldTeamData = getData(event.getPreviousTeam().get());
			TeamData newTeamData = getData(event.getTeam());

			if (event.getPreviousTeam().get() instanceof PlayerTeam && event.getTeam() instanceof PartyTeam && !((PartyTeam) event.getTeam()).isOwner(event.getPlayerId())) {
				newTeamData.mergeData(oldTeamData);
			}

			new TeamDataChangedMessage(new TeamDataUpdate(oldTeamData), new TeamDataUpdate(newTeamData)).sendToAll(server);
			new SyncTeamDataMessage(newTeamData, true).sendTo(event.getTeam().getOnlineMembers());
		}
	}
}
