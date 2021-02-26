package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class MessageDisplayItemRewardToast extends MessageBase {
	private final ItemStack stack;
	private final int count;

	MessageDisplayItemRewardToast(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
		count = buffer.readVarInt();
	}

	public MessageDisplayItemRewardToast(ItemStack is, int c) {
		stack = is;
		count = c;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
		buffer.writeVarInt(count);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.displayItemRewardToast(stack, count);
	}
}