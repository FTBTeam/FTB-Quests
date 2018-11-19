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
public class MessageEditObjectResponse extends MessageToClient
{
	private QuestObjectBase object;

	public MessageEditObjectResponse()
	{
	}

	public MessageEditObjectResponse(QuestObjectBase o)
	{
		object = o;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(object.uid);
		object.writeNetData(data);
	}

	@Override
	public void readData(DataIn data)
	{
		object = ClientQuestFile.INSTANCE.getBase(data.readInt());
		object.readNetData(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ClientQuestFile.INSTANCE.clearCachedData();
		object.editedFromGUI();
	}
}