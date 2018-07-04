package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataReader;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;

/**
 * @author LatvianModder
 */
public class ServerQuestList extends QuestList
{
	public static ServerQuestList INSTANCE;

	public boolean shouldSendUpdates = true;

	public static boolean load()
	{
		File file = new File(CommonUtils.folderConfig, "ftbquests/quests.json");
		JsonElement json;

		if (!file.exists())
		{
			json = new JsonObject();
		}
		else
		{
			json = DataReader.get(file).safeJson();
		}

		if (!json.isJsonObject())
		{
			return false;
		}
		else if (INSTANCE != null)
		{
			INSTANCE.invalidate();
		}

		INSTANCE = new ServerQuestList(json.getAsJsonObject());
		INSTANCE.save();
		return true;
	}

	private ServerQuestList(JsonObject json)
	{
		super(json);
	}

	public void sync(EntityPlayerMP player)
	{
		FTBQuestsTeamData data = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		NBTTagCompound taskDataTag = data.serializeTaskData();
		int[] claimedRewards = data.getClaimedRewards(player).toIntArray();
		new MessageSyncQuests(toJson(), data.team.getName(), taskDataTag, claimedRewards).sendTo(player);
	}

	public void save()
	{
		JsonUtils.toJsonSafe(new File(CommonUtils.folderConfig, "ftbquests/quests.json"), toJson());
	}
}