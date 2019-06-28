package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestData;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageToClient
{
	private short team;
	private int task;
	private long progress;

	public MessageUpdateTaskProgress()
	{
	}

	public MessageUpdateTaskProgress(short t, int k, long p)
	{
		team = t;
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
		data.writeShort(team);
		data.writeInt(task);
		data.writeVarLong(progress);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readShort();
		task = data.readInt();
		progress = data.readVarLong();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		Task qtask = ClientQuestFile.INSTANCE.getTask(task);

		if (qtask != null)
		{
			ClientQuestData data = ClientQuestFile.INSTANCE.getData(team);

			if (data != null)
			{
				ClientQuestFile.INSTANCE.clearCachedProgress();
				data.getTaskData(qtask).setProgress(progress);
			}
		}
	}
}