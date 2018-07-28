package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageMoveQuest extends MessageToServer
{
	private short id;
	private byte direction;

	public MessageMoveQuest()
	{
	}

	public MessageMoveQuest(short i, byte d)
	{
		id = i;
		direction = d;
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
		data.writeByte(direction);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
		direction = data.readByte();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (direction >= 0 && direction <= 7 && FTBQuests.canEdit(player))
		{
			Quest quest = ServerQuestList.INSTANCE.getQuest(id);

			if (quest != null)
			{
				quest.move(direction);
				ServerQuestList.INSTANCE.save();
				new MessageMoveQuestResponse(id, (byte) quest.x.getInt(), (byte) quest.y.getInt()).sendToAll();
			}
		}
	}
}