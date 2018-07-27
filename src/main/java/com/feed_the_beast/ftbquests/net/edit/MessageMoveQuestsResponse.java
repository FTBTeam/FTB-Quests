package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.gui.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageMoveQuestsResponse extends MessageToClient
{
	private int[] ids;
	private byte direction;

	public MessageMoveQuestsResponse()
	{
	}

	public MessageMoveQuestsResponse(int[] i, byte d)
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
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			if (direction >= 0 && direction <= 7)
			{
				for (int i : ids)
				{
					Quest quest = ClientQuestList.INSTANCE.getQuest(i);

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

				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					gui.quests.refreshWidgets();
				}
			}
		}
	}
}