package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageSetDep extends MessageToServer
{
	private short id;
	private short dep;
	private boolean add;

	public MessageSetDep()
	{
	}

	public MessageSetDep(short i, short d, boolean a)
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
		data.writeShort(id);
		data.writeShort(dep);
		data.writeBoolean(add);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
		dep = data.readShort();
		add = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			Quest quest = ServerQuestList.INSTANCE.getQuest(id);
			QuestObject d = ServerQuestList.INSTANCE.get(dep);

			if (quest != null && d instanceof ProgressingQuestObject && quest.setDependency(d.id, add))
			{
				ServerQuestList.INSTANCE.save();
				new MessageSetDepResponse(id, dep, add).sendToAll();
			}
		}
	}
}