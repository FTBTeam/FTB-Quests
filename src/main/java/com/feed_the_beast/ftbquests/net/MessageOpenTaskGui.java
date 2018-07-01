package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.gui.ContainerTaskBase;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageOpenTaskGui extends MessageToClient
{
	private int task;
	private int window;

	public MessageOpenTaskGui()
	{
	}

	public MessageOpenTaskGui(int t, int w)
	{
		task = t;
		window = w;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(task);
		data.writeInt(window);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readInt();
		window = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestTaskData data = ClientQuestList.INSTANCE.getQuestTaskData(task);
		ContainerTaskBase container = data.getContainer(ClientUtils.MC.player);

		if (container != null)
		{
			container.windowId = window;
			data.getGui(container).openGui();
		}
	}
}