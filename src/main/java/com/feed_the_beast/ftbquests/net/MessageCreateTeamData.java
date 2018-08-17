package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestProgress;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageCreateTeamData extends MessageToClient
{
	public String team;

	public MessageCreateTeamData()
	{
	}

	public MessageCreateTeamData(String t)
	{
		team = t;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(team);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestProgress data = new ClientQuestProgress(team);

			for (QuestTask task : ClientQuestFile.INSTANCE.allTasks)
			{
				data.createTaskData(task);
			}

			ClientQuestFile.INSTANCE.teamData.put(data.teamID, data);
		}
	}
}