package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageMoveQuestResponse extends MessageToClient
{
	private int id;
	private byte x, y;

	public MessageMoveQuestResponse()
	{
	}

	public MessageMoveQuestResponse(int i, byte _x, byte _y)
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
		data.writeInt(id);
		data.writeByte(x);
		data.writeByte(y);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		x = data.readByte();
		y = data.readByte();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest quest = ClientQuestFile.INSTANCE.getQuest(id);

			if (quest != null)
			{
				quest.x = x;
				quest.y = y;

				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					int sx = gui.quests.getScrollX();
					int sy = gui.quests.getScrollY();
					gui.quests.refreshWidgets();
					gui.quests.setScrollX(sx);
					gui.quests.setScrollY(sy);
				}
			}
		}
	}
}