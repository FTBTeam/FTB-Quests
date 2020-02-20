package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
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
		FTBQuests.NET_PROXY.moveQuest(id, chapter, x, y);
	}
}