package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.QuestTask;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;

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
		QuestTask t = ServerQuestFile.INSTANCE.getTask(task);

		if (t != null && t.quest.canStartTasks(teamData))
		{
			if (teamData.getQuestTaskData(t).submitTask(player, Collections.emptyList(), false))
			{
				player.inventory.markDirty();
				player.openContainer.detectAndSendChanges();
			}
		}
	}
}