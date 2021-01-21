package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageTogglePinnedResponse extends MessageBase
{
	private final int id;

	MessageTogglePinnedResponse(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageTogglePinnedResponse(int i)
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
		FTBQuests.NET_PROXY.togglePinned(id);
	}
}