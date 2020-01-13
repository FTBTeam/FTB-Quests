package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveQuestResponse extends MessageBase
{
	private final int id;
	private final int chapter;
	private final double x, y;

	MessageMoveQuestResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		chapter = buffer.readVarInt();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MessageMoveQuestResponse(int i, int c, double _x, double _y)
	{
		id = i;
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeVarInt(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			Quest quest = ClientQuestFile.INSTANCE.getQuest(id);

			if (quest != null)
			{
				quest.moved(x, y, chapter);
				GuiQuests gui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

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