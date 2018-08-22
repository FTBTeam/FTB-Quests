package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.util.FTBQuestsPlayerData;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import com.feed_the_beast.ftbquests.util.PlayerRewards;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile
{
	public static ServerQuestFile INSTANCE;

	public final File file;
	public boolean shouldSave = false;

	public ServerQuestFile(File f)
	{
		file = f;
	}

	public boolean load()
	{
		if (!file.exists())
		{
			NBTUtils.writeNBT(file, new NBTTagCompound());
		}

		NBTTagCompound nbt = NBTUtils.readNBT(file);
		readData(nbt == null ? new NBTTagCompound() : nbt);
		return nbt != null;
	}

	@Nullable
	@Override
	public IProgressData getData(String team)
	{
		if (Universe.loaded())
		{
			ForgeTeam t = Universe.get().getTeam(team);
			return t.isValid() ? FTBQuestsTeamData.get(t) : null;
		}

		return null;
	}

	@Override
	public Collection<FTBQuestsTeamData> getAllData()
	{
		if (Universe.loaded())
		{
			Collection<ForgeTeam> teams = Universe.get().getTeams();
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

		return Collections.emptyList();
	}

	@Override
	public PlayerRewards getRewards(EntityPlayer player)
	{
		return FTBQuestsPlayerData.get(Universe.get().getPlayer(player)).rewards;
	}

	public void save()
	{
		shouldSave = true;
		Universe.get().markDirty();
	}

	public void saveNow()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt);
		NBTUtils.writeNBTSafe(file, nbt);
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