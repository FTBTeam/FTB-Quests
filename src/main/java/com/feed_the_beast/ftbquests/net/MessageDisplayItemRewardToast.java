package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDisplayItemRewardToast extends MessageBase
{
	private final ItemStack stack;
	private final int count;

	MessageDisplayItemRewardToast(PacketBuffer buffer)
	{
		stack = buffer.readItemStack();
		count = buffer.readVarInt();
	}

	public MessageDisplayItemRewardToast(ItemStack is, int c)
	{
		stack = is;
		count = c;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeItemStack(stack);
		buffer.writeVarInt(count);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.displayItemRewardToast(stack, count);
	}
}