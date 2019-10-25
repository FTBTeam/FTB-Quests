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
	private int chapter;
	private double x, y;

	public MessageMoveQuestResponse()
	{
	}

	public MessageMoveQuestResponse(int i, int c, double _x, double _y)
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
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest quest = ClientQuestFile.INSTANCE.getQuest(id);

			if (quest != null)
			{
				quest.move(x, y, chapter);
				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null)
				{
					double sx = gui.questPanel.centerQuestX;
					double sy = gui.questPanel.centerQuestY;
					gui.questPanel.refreshWidgets();
					gui.questPanel.scrollTo(sx, sy);
				}
			}
		}
	}
}