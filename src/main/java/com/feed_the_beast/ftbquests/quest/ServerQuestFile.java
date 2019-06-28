package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObjectResponse;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import io.sommers.packmode.api.PackModeAPI;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile
{
	public static ServerQuestFile INSTANCE;

	public final Universe universe;
	public boolean shouldSave = false;
	private boolean isLoading = false;
	private File folder;

	public ServerQuestFile(Universe u)
	{
		universe = u;
	}

	private String getFolderName()
	{
		if (universe.world.getGameRules().hasRule("questfile"))
		{
			return universe.world.getGameRules().getString("questfile");
		}

		if (Loader.isModLoaded("packmode"))
		{
			return getPackmodeFolderName();
		}

		return "normal";
	}

	private static String getPackmodeFolderName()
	{
		return PackModeAPI.getInstance().getCurrentPackMode();
	}

	public void load()
	{
		folder = new File(Loader.instance().getConfigDir(), "ftbquests/" + getFolderName());

		if (folder.exists())
		{
			FTBQuests.LOGGER.info("Loading quests from " + folder.getAbsolutePath());
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}
	}

	@Override
	public boolean isClient()
	{
		return false;
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

	@Nullable
	@Override
	public QuestData getData(short team)
	{
		if (team == 0)
		{
			return null;
		}

		ForgeTeam t = universe.getTeam(team);
		return t.isValid() ? ServerQuestData.get(t) : null;
	}

	@Nullable
	@Override
	public QuestData getData(String team)
	{
		if (team.isEmpty())
		{
			return null;
		}

		ForgeTeam t = universe.getTeam(team);
		return t.isValid() ? ServerQuestData.get(t) : null;
	}

	@Override
	public Collection<ServerQuestData> getAllData()
	{
		Collection<ForgeTeam> teams = universe.getTeams();
		List<ServerQuestData> list = new ArrayList<>(teams.size());

		for (ForgeTeam team : teams)
		{
			if (team.isValid())
			{
				list.add(ServerQuestData.get(team));
			}
		}

		return list;
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
				FileUtils.deleteSafe(file);
			}
		}

		new MessageDeleteObjectResponse(id).sendToAll();
	}

	public void save()
	{
		shouldSave = true;
		universe.markDirty();
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