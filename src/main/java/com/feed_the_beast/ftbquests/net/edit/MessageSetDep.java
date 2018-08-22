package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageSetDep extends MessageToServer
{
	private String id;
	private String dep;
	private boolean add;

	public MessageSetDep()
	{
	}

	public MessageSetDep(String i, String d, boolean a)
	{
		id = i;
		dep = d;
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
		data.writeString(id);
		data.writeString(dep);
		data.writeBoolean(add);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
		dep = data.readString();
		add = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			Quest quest = ServerQuestFile.INSTANCE.getQuest(id);
			QuestObject d = ServerQuestFile.INSTANCE.get(dep);

			if (quest != null && d != null && quest.setDependency(d, add))
			{
				ServerQuestFile.INSTANCE.save();
				new MessageSetDepResponse(id, dep, add).sendToAll();
			}
		}
	}
}