package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
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

	public void sync(EntityPlayerMP player)
	{
		FTBQuestsTeamData data = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		NBTTagCompound taskDataTag = data.serializeTaskData();
		int[] claimedRewards = data.getClaimedRewards(player).toIntArray();
		new MessageSyncQuests(toNBT(), data.team.getName(), taskDataTag, claimedRewards).sendTo(player);
	}

	public void save()
	{
		NBTUtils.writeNBTSafe(new File(CommonUtils.folderConfig, "ftbquests/quests.nbt"), toNBT());
	}
}