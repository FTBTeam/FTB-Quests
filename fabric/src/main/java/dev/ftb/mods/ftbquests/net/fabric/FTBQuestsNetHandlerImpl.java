package dev.ftb.mods.ftbquests.net.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface FTBQuestsNetHandlerImpl {
	static void writeItemType(FriendlyByteBuf buffer, ItemStack stack) {
		buffer.writeItem(stack);
	}

	static ItemStack readItemType(FriendlyByteBuf buffer) {
		return buffer.readItem();
	}
}
