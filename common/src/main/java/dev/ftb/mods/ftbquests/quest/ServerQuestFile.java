package dev.ftb.mods.ftbquests.quest;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.net.MessageCreateTeamData;
import dev.ftb.mods.ftbquests.net.MessageDeleteObjectResponse;
import dev.ftb.mods.ftbquests.net.MessageSyncQuests;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import dev.ftb.mods.ftbquests.util.FileUtils;
import dev.ftb.mods.ftbquests.util.NBTUtils;
import me.shedaniel.architectury.hooks.LevelResourceHooks;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.nbt.CompoundTag;
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
	public static final LevelResource FTBQUESTS_DATA = LevelResourceHooks.create("ftbquests");

	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private Path folder;

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
				Files.list(path).filter(p -> !p.getFileName().toString().contains("-")).forEach(path1 -> {
					CompoundTag nbt = NBTUtils.readSNBT(path1);

					try {
						UUID uuid = UUIDTypeAdapter.fromString(nbt.getString("uuid"));
						TeamData data = new TeamData(this, uuid);
						addData(data, true);
						data.deserializeNBT(nbt);
					} catch (Exception ex) {
						ex.printStackTrace();
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

		new MessageDeleteObjectResponse(id).sendToAll();
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
				NBTUtils.writeSNBT(path.resolve(UUIDTypeAdapter.fromUUID(data.uuid) + ".snbt"), data.serializeNBT());
				data.shouldSave = false;
			}
		}
	}

	public void unload() {
		saveNow();
		deleteChildren();
		deleteSelf();
	}

	public void onLoggedIn(ServerPlayer player) {
		UUID id = player.getUUID();
		TeamData data = teamDataMap.get(id);

		if (data == null) {
			data = new TeamData(this, id);
			data.save();
		}

		if (!data.name.equals(player.getGameProfile().getName())) {
			data.name = player.getGameProfile().getName();
			data.save();
		}

		addData(data, false);

		for (ServerPlayer player1 : server.getPlayerList().getPlayers()) {
			if (player1 != player) {
				new MessageCreateTeamData(data).sendTo(player1);
			}
		}

		new MessageSyncQuests(id, this).sendTo(player);
		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		for (ChapterGroup group : ServerQuestFile.INSTANCE.chapterGroups) {
			for (Chapter chapter : group.chapters) {
				for (Quest quest : chapter.quests) {
					data.checkAutoCompletion(quest);
				}
			}
		}
	}
}