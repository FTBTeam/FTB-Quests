package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.FTBQuestsWorldData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageChangeID extends MessageToServer
{
	private int id;
	private String newId;

	public MessageChangeID()
	{
	}

	public MessageChangeID(int i, String n)
	{
		id = i;
		newId = n;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
		data.writeString(newId);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		newId = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.get(id);

			if (object != null && object.getObjectType() != QuestObjectType.FILE)
			{
				object.id = newId;
				object.clearCachedData();
				ServerQuestFile.INSTANCE.refreshIDMap();

				for (ForgeTeam team : FTBQuestsWorldData.INSTANCE.universe.getTeams())
				{
					team.markDirty();
				}

				new MessageChangeIDResponse(id, newId).sendToAll();
				ServerQuestFile.INSTANCE.save();
			}
		}
	}
}