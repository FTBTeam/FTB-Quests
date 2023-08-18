package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class DisplayItemRewardToastMessage extends BaseS2CMessage {
	private final ItemStack stack;
	private final int count;

	DisplayItemRewardToastMessage(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
		count = buffer.readVarInt();
	}

	public DisplayItemRewardToastMessage(ItemStack is, int c) {
		stack = is;
		count = c;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.DISPLAY_ITEM_REWARD_TOAST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
		buffer.writeVarInt(count);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.displayItemRewardToast(stack, count);
	}
}