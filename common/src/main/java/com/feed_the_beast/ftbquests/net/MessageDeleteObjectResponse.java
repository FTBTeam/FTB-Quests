package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageBase
{
	private final int id;

	MessageDeleteObjectResponse(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDeleteObjectResponse(int i)
	{
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.deleteObject(id);
	}
}