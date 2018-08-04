package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageDeleteObject extends MessageToServer
{
	private short id;

	public MessageDeleteObject()
	{
	}

	public MessageDeleteObject(short i)
	{
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (id != 0 && FTBQuests.canEdit(player))
		{
			QuestObject object = ServerQuestFile.INSTANCE.get(id);

			if (object != null)
			{
				object.deleteChildren();
				object.deleteSelf();
				ServerQuestFile.INSTANCE.save();
			}

			new MessageDeleteObjectResponse(id).sendToAll();
		}
	}
}