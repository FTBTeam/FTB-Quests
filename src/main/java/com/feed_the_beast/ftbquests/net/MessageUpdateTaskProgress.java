package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private int task;
	private int progress;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(int k, int p)
	{
		task = k;
		progress = p;
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
		data.writeInt(progress);
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readInt();
		progress = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		ClientQuestList.INSTANCE.getQuestTaskData(task).setProgress(progress, false);
	}
}