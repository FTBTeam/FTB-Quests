package com.feed_the_beast.ftbquests.net.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class FTBQuestsNetHandlerImpl
{
	public static void writeItemType(FriendlyByteBuf buffer, ItemStack stack)
	{
		buffer.writeItem(stack);
	}

	public static ItemStack readItemType(FriendlyByteBuf buffer)
	{
		return buffer.readItem();
	}
}
