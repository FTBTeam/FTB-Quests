package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageEditDependencyResponse extends MessageToClient
{
	private String quest;
	private String object;
	private boolean add;

	public MessageEditDependencyResponse()
	{
	}

	public MessageEditDependencyResponse(String q, String o, boolean a)
	{
		quest = q;
		object = o;
		add = a;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(quest);
		data.writeString(object);
		data.writeBoolean(add);
	}

	@Override
	public void readData(DataIn data)
	{
		quest = data.readString();
		object = data.readString();
		add = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest q = ClientQuestFile.INSTANCE.getQuest(quest);

			if (q != null)
			{
				QuestObject o = ClientQuestFile.INSTANCE.get(object);

				if (o != null && (add ? q.dependencies.add(o.getID()) : q.dependencies.remove(o.getID())))
				{
					q.clearCachedData();
					q.verifyDependencies();
					ClientQuestFile.INSTANCE.refreshGui();
				}
			}
		}
	}
}