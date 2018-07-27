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
public class MessageMoveQuests extends MessageToServer
{
	private int[] ids;
	private byte direction;

	public MessageMoveQuests()
	{
	}

	public MessageMoveQuests(int[] i, byte d)
	{
		ids = i;
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
		data.writeShort(ids.length);

		for (int i : ids)
		{
			data.writeInt(i);
		}

		data.writeByte(direction);
	}

	@Override
	public void readData(DataIn data)
	{
		ids = new int[data.readUnsignedShort()];

		for (int i = 0; i < ids.length; i++)
		{
			ids[i] = data.readInt();
		}

		direction = data.readByte();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (direction >= 0 && direction <= 7 && FTBQuests.canEdit(player))
		{
			for (int i : ids)
			{
				Quest quest = ServerQuestList.INSTANCE.getQuest(i);

				if (quest != null)
				{
					if (direction == 5 || direction == 6 || direction == 7)
					{
						int v = quest.x.getInt() - 1;
						quest.x.setInt(v <= -128 ? 127 : v);
					}

					if (direction == 1 || direction == 2 || direction == 3)
					{
						int v = quest.x.getInt() + 1;
						quest.x.setInt(v >= 128 ? -127 : v);
					}

					if (direction == 0 || direction == 1 || direction == 7)
					{
						int v = quest.y.getInt() - 1;
						quest.y.setInt(v <= -128 ? 127 : v);
					}

					if (direction == 3 || direction == 4 || direction == 5)
					{
						int v = quest.y.getInt() + 1;
						quest.y.setInt(v >= 128 ? -127 : v);
					}
				}
			}

			ServerQuestList.INSTANCE.save();
			new MessageMoveQuestsResponse(ids, direction).sendToAll();
		}
	}
}