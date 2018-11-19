package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageToClient
{
	private int id;

	public MessageDeleteObjectResponse()
	{
	}

	public MessageDeleteObjectResponse(int i)
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
		data.writeInt(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				object.deleteChildren();
				object.deleteSelf();
				ClientQuestFile.INSTANCE.refreshIDMap();
				object.editedFromGUI();
			}
		}
	}
}