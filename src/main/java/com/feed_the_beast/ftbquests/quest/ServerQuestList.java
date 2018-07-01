package com.feed_the_beast.ftbquests.quest;

import com.feed_the_beast.ftblib.events.ServerReloadEvent;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataReader;
import com.feed_the_beast.ftblib.lib.util.CommonUtils;
import com.feed_the_beast.ftblib.lib.util.JsonUtils;
import com.feed_the_beast.ftbquests.net.MessageSyncQuests;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
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

	public static boolean reload(ServerReloadEvent event)
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
		INSTANCE.sendToAll();
		return true;
	}

	private ServerQuestList(JsonObject json)
	{
		super(json);
	}

	private void sendTo0(JsonElement json, EntityPlayerMP player)
	{
		FTBQuestsTeamData data = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		NBTTagCompound taskDataTag = new NBTTagCompound();

		for (QuestTaskData data1 : data.taskData.values())
		{
			NBTTagCompound nbt1 = new NBTTagCompound();
			data1.writeToNBT(nbt1);

			if (!nbt1.hasNoTags())
			{
				taskDataTag.setTag(Integer.toString(data1.task.id), nbt1);
			}
		}

		int[] claimedRewards = data.getClaimedRewards(player).toIntArray();
		new MessageSyncQuests(json, data.team.getName(), taskDataTag, claimedRewards).sendTo(player);
	}

	public void sendTo(EntityPlayerMP player)
	{
		sendTo0(toJson(), player);
	}

	public void sendToAll()
	{
		JsonElement json = toJson();

		for (EntityPlayerMP player : Universe.get().server.getPlayerList().getPlayers())
		{
			sendTo0(json, player);
		}
	}

	public void save()
	{
		JsonUtils.toJsonSafe(new File(CommonUtils.folderConfig, "ftbquests/quests.json"), toJson());
	}
}