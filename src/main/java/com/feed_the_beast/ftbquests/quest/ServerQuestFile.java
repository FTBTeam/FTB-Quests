package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageCreatePlayerData;
import com.feed_the_beast.ftbquests.net.MessageDeleteObjectResponse;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.ftbquests.util.FileUtils;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile
{
	public static final FolderName FTBQUESTS_DATA = new FolderName("ftbquests");

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
	}

	public void load()
	{
		folder = FMLPaths.CONFIGDIR.get().resolve("ftbquests/quests");

		if (Files.exists(folder))
		{
			FTBQuests.LOGGER.info("Loading quests from " + folder);
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}

		int c = chapters.size();
		int q = 0;
		int t = 0;
		int r = 0;

		for (Chapter chapter : chapters)
		{
			q += chapter.quests.size();

			for (Quest quest : chapter.quests)
			{
				t += quest.tasks.size();
				r += quest.rewards.size();
			}
		}

		FTBQuests.LOGGER.info(String.format("Loaded %d chapters, %d quests, %d tasks and %d rewards. In total, %d objects", c, q, t, r, getAllObjects().size()));

		Path path = server.func_240776_a_(FTBQUESTS_DATA);

		if (Files.exists(path))
		{
			try
			{
				Files.list(path).forEach(path1 -> {
					CompoundNBT nbt = NBTUtils.readSNBT(path1);

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
	public LogicalSide getSide()
	{
		return LogicalSide.SERVER;
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

		Path path = server.func_240776_a_(FTBQUESTS_DATA);

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

	public void onLoggedIn(ServerPlayerEntity player)
	{
		UUID id = player.getUniqueID();
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

		for (ServerPlayerEntity player1 : server.getPlayerList().getPlayers())
		{
			if (player1 != player)
			{
				new MessageCreatePlayerData(data).sendTo(player1);
			}
		}

		new MessageSyncQuests(id, this).sendTo(player);
		player.container.addListener(new FTBQuestsInventoryListener(player));

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				data.checkAutoCompletion(quest);
			}
		}
	}

	@Override
	public boolean updateChapterIndices()
	{
		if (super.updateChapterIndices())
		{
			save();
			return true;
		}

		return false;
	}
}