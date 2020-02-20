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

	MessageDisplayItemRewardToast(PacketBuffer buffer)
	{
		stack = buffer.readItemStack();
	}

	public MessageDisplayItemRewardToast(ItemStack is)
	{
		stack = is;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeItemStack(stack);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.displayItemRewardToast(stack);
	}
}