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
public class MessageTogglePinnedResponse extends MessageToClient
{
	private int id;

	public MessageTogglePinnedResponse()
	{
	}

	public MessageTogglePinnedResponse(int i)
	{
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
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
			if (ClientQuestFile.INSTANCE.pinnedQuests.contains(id))
			{
				ClientQuestFile.INSTANCE.pinnedQuests.rem(id);
			}
			else
			{
				ClientQuestFile.INSTANCE.pinnedQuests.add(id);
			}

			ClientQuestFile.INSTANCE.questTreeGui.otherButtonsBottomPanel.refreshWidgets();

			if (ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel != null)
			{
				ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
			}
		}
	}
}