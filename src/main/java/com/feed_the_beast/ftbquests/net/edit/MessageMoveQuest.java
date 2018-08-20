package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageMoveQuest extends MessageToServer
{
	private String id;
	private byte x, y;

	public MessageMoveQuest()
	{
	}

	public MessageMoveQuest(String i, byte _x, byte _y)
	{
		id = i;
		x = _x;
		y = _y;
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
		data.writeByte(x);
		data.writeByte(y);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
		x = data.readByte();
		y = data.readByte();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (x >= -Quest.POS_LIMIT && x <= Quest.POS_LIMIT && y >= -Quest.POS_LIMIT && y <= Quest.POS_LIMIT && FTBQuests.canEdit(player))
		{
			Quest quest = ServerQuestFile.INSTANCE.getQuest(id);

			if (quest != null)
			{
				quest.x = x;
				quest.y = y;
				ServerQuestFile.INSTANCE.save();
				new MessageMoveQuestResponse(id, x, y).sendToAll();
			}
		}
	}
}