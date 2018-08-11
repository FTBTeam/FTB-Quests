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
	private String id;

	public MessageDeleteObject()
	{
	}

	public MessageDeleteObject(String i)
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
		data.writeString(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (!id.isEmpty() && FTBQuests.canEdit(player))
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