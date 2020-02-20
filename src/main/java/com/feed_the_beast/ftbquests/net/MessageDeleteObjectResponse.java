package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageBase
{
	private final int id;

	MessageDeleteObjectResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDeleteObjectResponse(int i)
	{
		id = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.deleteObject(id);
	}
}