package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.FTBQuestsConfig;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.server.permission.PermissionAPI;

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
public class ServerQuestList extends QuestList
{
	public static ServerQuestList INSTANCE;

	public boolean shouldSendUpdates = true;
	private Random random;

	public static boolean load()
	{
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

		if (nbt == null)
		{
			return false;
		}
		else if (INSTANCE != null)
		{
			INSTANCE.invalidate();
		}

		INSTANCE = new ServerQuestList(nbt);
		INSTANCE.save();
		return true;
	}

	private ServerQuestList(NBTTagCompound nbt)
	{
		super(nbt);
	}

	@Override
	public int requestID()
	{
		if (random == null)
		{
			random = new Random();
		}

		int id;

		do
		{
			id = random.nextInt(Integer.MAX_VALUE - 1) + 1;
		}
		while (get(id) != null);

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
		int[] claimedRewards = data.getClaimedRewards(player).toIntArray();
		boolean e = FTBQuestsConfig.general.editing_mode && PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT);
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt);
		new MessageSyncQuests(nbt, data.team.getName(), taskDataTag, claimedRewards, e).sendTo(player);
	}

	public void syncAll()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt);

		for (EntityPlayerMP player : Universe.get().server.getPlayerList().getPlayers())
		{
			FTBQuestsTeamData data = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
			NBTTagCompound taskDataTag = data.serializeTaskData();
			int[] claimedRewards = data.getClaimedRewards(player).toIntArray();
			boolean e = FTBQuestsConfig.general.editing_mode && PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT);
			new MessageSyncQuests(nbt, data.team.getName(), taskDataTag, claimedRewards, e).sendTo(player);
		}
	}

	public void save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeData(nbt);
		NBTUtils.writeNBTSafe(new File(CommonUtils.folderConfig, "ftbquests/quests.nbt"), nbt);
	}
}