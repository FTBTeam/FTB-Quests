package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageTogglePinnedResponse extends MessageBase
{
	private final long id;

	MessageTogglePinnedResponse(FriendlyByteBuf buffer)
	{
		id = buffer.readLong();
	}

	public MessageTogglePinnedResponse(long i)
	{
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.togglePinned(id);
	}
}