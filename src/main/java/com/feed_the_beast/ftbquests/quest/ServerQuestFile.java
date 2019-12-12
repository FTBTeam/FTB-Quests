package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageDeleteObjectResponse;
import com.feed_the_beast.ftbquests.util.FileUtils;
import com.teamacronymcoders.packmode.api.PackModeAPI;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile
{
	public static ServerQuestFile INSTANCE;

	public final MinecraftServer server;
	public boolean shouldSave = false;
	private boolean isLoading = false;
	private File folder;

	public ServerQuestFile(MinecraftServer s)
	{
		server = s;
	}

	private String getFolderName()
	{
		if (ModList.get().isLoaded("packmode"))
		{
			return getPackmodeFolderName();
		}

		return "normal";
	}

	private static String getPackmodeFolderName()
	{
		return PackModeAPI.getInstance().getPackMode();
	}

	public void load()
	{
		folderName = getFolderName();
		folder = FMLPaths.CONFIGDIR.get().resolve("ftbquests/" + folderName).toFile();

		if (folder.exists())
		{
			FTBQuests.LOGGER.info("Loading quests from " + folder.getAbsolutePath());
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
	public File getFolder()
	{
		return folder;
	}

	@Override
	public void deleteObject(int id)
	{
		QuestObjectBase object = getBase(id);

		if (object != null)
		{
			File file = object.getFile();

			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			save();

			if (file != null)
			{
				FileUtils.delete(file);
			}
		}

		new MessageDeleteObjectResponse(id).sendToAll();
	}

	public void save()
	{
		shouldSave = true;
		//FIXME: universe.markDirty();
	}

	public void saveNow()
	{
		writeDataFull(getFolder());
	}

	public void unload()
	{
		if (shouldSave)
		{
			saveNow();
			shouldSave = false;
		}

		deleteChildren();
		deleteSelf();
	}
}