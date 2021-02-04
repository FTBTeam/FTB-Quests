package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageCreatePlayerData;
import com.feed_the_beast.ftbquests.net.MessageDeleteObjectResponse;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.reward.RewardTypes;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.ftbquests.util.FileUtils;
import com.feed_the_beast.ftbquests.util.NBTUtils;
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
public class ServerQuestFile extends QuestFile
{
	public static final LevelResource FTBQUESTS_DATA = LevelResourceHooks.create("ftbquests");

	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	private boolean shouldSave;
	private boolean isLoading;
	private Path folder;

	public ServerQuestFile(MinecraftServer s)
	{
		server = s;
		shouldSave = false;
		isLoading = false;

		int taskTypeId = 0;

		for (TaskType type : TaskTypes.TYPES.values())
		{
			type.intId = ++taskTypeId;
			taskTypeIds.put(type.intId, type);
		}

		int rewardTypeId = 0;

		for (RewardType type : RewardTypes.TYPES.values())
		{
			type.intId = ++rewardTypeId;
			rewardTypeIds.put(type.intId, type);
		}
	}

	public void load()
	{
		folder = Platform.getConfigFolder().resolve("ftbquests/quests");

		if (Files.exists(folder))
		{
			FTBQuests.LOGGER.info("Loading quests from " + folder);
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}

		Path path = server.getWorldPath(FTBQUESTS_DATA);

		if (Files.exists(path))
		{
			try
			{
				Files.list(path).forEach(path1 -> {
					CompoundTag nbt = NBTUtils.readSNBT(path1);

					try
					{
						UUID uuid = UUID.fromString(nbt.getString("uuid"));
						PlayerData data = new PlayerData(this, uuid);
						addData(data, true);
						data.deserializeNBT(nbt);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				});
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	@Override
	public Env getSide()
	{
		return Env.SERVER;
	}

	@Override
	public boolean isLoading()
	{
		return isLoading;
	}

	@Override
	public Path getFolder()
	{
		return folder;
	}

	@Override
	public void deleteObject(int id)
	{
		QuestObjectBase object = getBase(id);

		if (object != null)
		{
			String file = object.getPath();

			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			save();

			if (file != null)
			{
				FileUtils.delete(getFolder().resolve(file).toFile());
			}
		}

		new MessageDeleteObjectResponse(id).sendToAll();
	}

	@Override
	public void save()
	{
		shouldSave = true;
	}

	public void saveNow()
	{
		if (shouldSave)
		{
			writeDataFull(getFolder());
			shouldSave = false;
		}

		Path path = server.getWorldPath(FTBQUESTS_DATA);

		for (PlayerData data : getAllData())
		{
			if (data.shouldSave)
			{
				NBTUtils.writeSNBT(path.resolve(data.uuid.toString() + ".snbt"), data.serializeNBT());
				data.shouldSave = false;
			}
		}
	}

	public void unload()
	{
		saveNow();
		deleteChildren();
		deleteSelf();
	}

	public void onLoggedIn(ServerPlayer player)
	{
		UUID id = player.getUUID();
		PlayerData data = playerDataMap.get(id);

		if (data == null)
		{
			data = new PlayerData(this, id);
			data.save();
		}

		if (!data.name.equals(player.getGameProfile().getName()))
		{
			data.name = player.getGameProfile().getName();
			data.save();
		}

		addData(data, false);

		for (ServerPlayer player1 : server.getPlayerList().getPlayers())
		{
			if (player1 != player)
			{
				new MessageCreatePlayerData(data).sendTo(player1);
			}
		}

		new MessageSyncQuests(id, this).sendTo(player);
		player.inventoryMenu.addSlotListener(new FTBQuestsInventoryListener(player));

		for (ChapterGroup group : ServerQuestFile.INSTANCE.chapterGroups)
		{
			for (Chapter chapter : group.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					data.checkAutoCompletion(quest);
				}
			}
		}
	}
}