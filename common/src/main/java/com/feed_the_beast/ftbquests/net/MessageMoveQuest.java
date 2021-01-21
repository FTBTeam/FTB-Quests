package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveQuest extends MessageBase
{
	private final int id;
	private final int chapter;
	private final double x, y;

	MessageMoveQuest(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
		chapter = buffer.readVarInt();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MessageMoveQuest(int i, int c, double _x, double _y)
	{
		id = i;
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeVarInt(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
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