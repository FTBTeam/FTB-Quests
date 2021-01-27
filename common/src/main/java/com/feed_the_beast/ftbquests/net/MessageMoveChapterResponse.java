package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterResponse extends MessageBase
{
	private final int id;
	private final boolean up;

	MessageMoveChapterResponse(FriendlyByteBuf buffer)
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
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.moveChapter(id, up);
	}
}