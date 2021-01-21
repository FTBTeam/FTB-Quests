package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageSyncEditingMode extends MessageBase
{
	private final boolean editingMode;

	public MessageSyncEditingMode(FriendlyByteBuf buffer)
	{
		editingMode = buffer.readBoolean();
	}

	public MessageSyncEditingMode(boolean e)
	{
		editingMode = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(editingMode);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.syncEditingMode(editingMode);
	}
}