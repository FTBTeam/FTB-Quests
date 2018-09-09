package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageSubmitAllItems extends MessageToServer
{
	private String quest;

	public MessageSubmitAllItems()
	{
	}

	public MessageSubmitAllItems(String q)
	{
		quest = q;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(quest);
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);
		Quest q = ServerQuestFile.INSTANCE.getQuest(quest);

		if (q != null && q.canStartTasks(teamData))
		{
			boolean changed = false;

			for (QuestTask t : q.tasks)
			{
				if (teamData.getQuestTaskData(t).submitItems(player))
				{
					changed = true;
				}
			}

			if (changed)
			{
				player.inventory.markDirty();
				player.openContainer.detectAndSendChanges();
			}
		}
	}
}