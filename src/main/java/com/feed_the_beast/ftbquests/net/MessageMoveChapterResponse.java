package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterResponse extends MessageBase
{
	private final int id;
	private final boolean up;

	MessageMoveChapterResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
		up = buffer.readBoolean();
	}

	public MessageMoveChapterResponse(int i, boolean u)
	{
		id = i;
		up = u;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.moveChapter(id, up);
	}
}