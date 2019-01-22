package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.FileUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.edit.MessageDeleteObjectResponse;
import com.feed_the_beast.ftbquests.quest.task.KillTask;
import com.feed_the_beast.ftbquests.quest.task.LocationTask;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import io.sommers.packmode.api.PackModeAPI;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
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

	private List<KillTask> killTasks = null;
	private List<LocationTask> locationTasks = null;

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
		File folder = new File(Loader.instance().getConfigDir(), "ftbquests/" + getFolderName());

		if (folder.exists())
		{
			FTBQuests.LOGGER.info("Loading quests from " + folder.getAbsolutePath());
			isLoading = true;
			readDataFull(folder);
			isLoading = false;
		}
		else
		{
			File old = new File(universe.server.getDataDirectory(), "questpacks/" + getFolderName() + ".nbt");

			if (old.exists())
			{
				NBTTagCompound nbt = NBTUtils.readNBT(old);

				if (nbt != null)
				{
					FTBQuests.LOGGER.info("Loading old quests file from " + old.getAbsolutePath());
					isLoading = true;
					readDataOld(nbt);
					isLoading = false;
					FileUtils.delete(old);

					if (!(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
					{
						File[] p = old.getParentFile().listFiles();

						if (p == null || p.length == 0)
						{
							FileUtils.delete(old.getParentFile());
						}
					}
				}
			}
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

	@Nullable
	@Override
	public ITeamData getData(short team)
	{
		if (team == 0)
		{
			return null;
		}

		ForgeTeam t = universe.getTeam(team);
		return t.isValid() ? FTBQuestsTeamData.get(t) : null;
	}

	@Nullable
	@Override
	public ITeamData getData(String team)
	{
		if (team.isEmpty())
		{
			return null;
		}

		ForgeTeam t = universe.getTeam(team);
		return t.isValid() ? FTBQuestsTeamData.get(t) : null;
	}

	@Override
	public Collection<FTBQuestsTeamData> getAllData()
	{
		Collection<ForgeTeam> teams = universe.getTeams();
		List<FTBQuestsTeamData> list = new ArrayList<>(teams.size());

		for (ForgeTeam team : teams)
		{
			if (team.isValid())
			{
				list.add(FTBQuestsTeamData.get(team));
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
			object.deleteChildren();
			object.deleteSelf();
			refreshIDMap();
			save();

			File file = object.getFile(new File(Loader.instance().getConfigDir(), "ftbquests/" + getFolderName()));

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
		writeDataFull(new File(Loader.instance().getConfigDir(), "ftbquests/" + getFolderName()));
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

	@Override
	public void clearCachedData()
	{
		super.clearCachedData();
		killTasks = null;
		locationTasks = null;
	}

	public final List<KillTask> getKillTasks()
	{
		if (killTasks == null)
		{
			killTasks = new ArrayList<>();

			for (QuestChapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						if (task instanceof KillTask)
						{
							killTasks.add((KillTask) task);
						}
					}
				}
			}
		}

		return killTasks;
	}

	public final List<LocationTask> getLocationTasks()
	{
		if (locationTasks == null)
		{
			locationTasks = new ArrayList<>();

			for (QuestChapter chapter : chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (QuestTask task : quest.tasks)
					{
						if (task instanceof LocationTask)
						{
							locationTasks.add((LocationTask) task);
						}
					}
				}
			}
		}

		return locationTasks;
	}
}