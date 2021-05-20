package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class DisplayItemRewardToastPacket extends BaseS2CPacket {
	private final ItemStack stack;
	private final int count;

	DisplayItemRewardToastPacket(FriendlyByteBuf buffer) {
		stack = buffer.readItem();
		count = buffer.readVarInt();
	}

	public DisplayItemRewardToastPacket(ItemStack is, int c) {
		stack = is;
		count = c;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.DISPLAY_ITEM_REWARD_TOAST;
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