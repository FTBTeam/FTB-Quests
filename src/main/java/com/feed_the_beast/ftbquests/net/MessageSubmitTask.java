package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageSubmitTask extends MessageToServer
{
	private int task;

	public MessageSubmitTask()
	{
	}

	public MessageSubmitTask(int t)
	{
		task = t;
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
	}

	@Override
	public void readData(DataIn data)
	{
		task = data.readInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		ServerQuestData teamData = ServerQuestData.get(Universe.get().getPlayer(player).team);
		Task t = ServerQuestFile.INSTANCE.getTask(task);

		if (t != null && t.canInsertItem() && t.quest.canStartTasks(teamData))
		{
			teamData.getTaskData(t).submitTask(player);
		}
	}
}