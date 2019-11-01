package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageMoveQuest extends MessageToServer
{
	private int id;
	private int chapter;
	private double x, y;

	public MessageMoveQuest()
	{
	}

	public MessageMoveQuest(int i, int c, double _x, double _y)
	{
		id = i;
		chapter = c;
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
		data.writeInt(id);
		data.writeInt(chapter);
		data.writeDouble(x);
		data.writeDouble(y);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		chapter = data.readInt();
		x = data.readDouble();
		y = data.readDouble();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		Quest quest = ServerQuestFile.INSTANCE.getQuest(id);

		if (quest != null)
		{
			quest.moved(x, y, chapter);
			ServerQuestFile.INSTANCE.save();
			new MessageMoveQuestResponse(id, chapter, x, y).sendToAll();
		}
	}
}