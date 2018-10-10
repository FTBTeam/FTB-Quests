package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageRepeatQuest extends MessageToClient
{
	public String quest;

	public MessageRepeatQuest()
	{
	}

	public MessageRepeatQuest(String t)
	{
		quest = t;
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
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.existsWithTeam())
		{
			Quest q = ClientQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				q.resetProgress(ClientQuestFile.INSTANCE.self, false);
			}
		}
	}
}