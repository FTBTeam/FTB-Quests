package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author LatvianModder
 */
public class ServerQuestFile extends QuestFile
{
	public static ServerQuestFile INSTANCE;

	public boolean shouldSendUpdates = true;
	private Random random;
	public boolean shouldSave = false;

	public static boolean load()
	{
		if (INSTANCE != null)
		{
			INSTANCE.invalidate();
		}

		File file = new File(CommonUtils.folderConfig, "ftbquests/quests.nbt");
		NBTTagCompound nbt;

		if (!file.exists())
		{
			nbt = new NBTTagCompound();
		}
		else
		{
			nbt = NBTUtils.readNBT(file);
		}

		if (nbt != null)
		{
			INSTANCE = new ServerQuestFile(nbt);
			INSTANCE.save();
			return true;
		}
		else
		{
			INSTANCE = new ServerQuestFile(new NBTTagCompound());
			return false;
		}
	}

	private ServerQuestFile(NBTTagCompound nbt)
	{
		super(nbt);
	}

	@Override
	public short requestID()
	{
		if (random == null)
		{
			random = new Random();
		}

		short id;

		do
		{
			id = (short) (1 + random.nextInt(MAX_ID));
		}
		while (map.containsKey(id));

		return id;
	}

	@Nullable
	@Override
	public IProgressData getData(String owner)
	{
		if (Universe.loaded())
		{
			ForgeTeam team = Universe.get().getTeam(owner);
			return team.isValid() ? FTBQuestsTeamData.get(team) : null;
		}

		return null;
	}

	@Override
	public Collection<IProgressData> getAllData()
	{
		if (Universe.loaded())
		{
			Collection<ForgeTeam> teams = Universe.get().getTeams();
			List<IProgressData> list = new ArrayList<>(teams.size());

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

	public void sync(EntityPlayerMP player)
	{
		FTBQuestsTeamData data = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		NBTTagCompound taskDataTag = data.serializeTaskData();
		short[] claimedRewards = data.getClaimedRewards(player).toShortArray();
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt);
		new MessageSyncQuests(nbt, data.team.getName(), taskDataTag, claimedRewards, FTBQuests.canEdit(player)).sendTo(player);
	}

	public void save()
	{
		shouldSave = true;
		Universe.get().markDirty();
	}
}