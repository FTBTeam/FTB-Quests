package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageToClient
{
	private short id;

	public MessageDeleteObjectResponse()
	{
	}

	public MessageDeleteObjectResponse(short i)
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
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			QuestObject object = ClientQuestList.INSTANCE.get(id);

			if (object != null)
			{
				object.deleteChildren();
				object.deleteSelf();
				ClientQuestList.INSTANCE.refreshGui(ClientQuestList.INSTANCE);
			}
		}
	}
}