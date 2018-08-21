package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageSyncEditingMode extends MessageToClient
{
	public boolean editingMode;

	public MessageSyncEditingMode()
	{
	}

	public MessageSyncEditingMode(boolean e)
	{
		editingMode = e;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeBoolean(editingMode);
	}

	@Override
	public void readData(DataIn data)
	{
		editingMode = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.editingMode != editingMode)
		{
			ClientQuestFile.INSTANCE.editingMode = editingMode;
			ClientQuestFile.INSTANCE.refreshGui(ClientQuestFile.INSTANCE);
		}
	}
}