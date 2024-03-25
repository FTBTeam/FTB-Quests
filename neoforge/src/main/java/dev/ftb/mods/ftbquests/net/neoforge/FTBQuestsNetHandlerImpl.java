package dev.ftb.mods.ftbquests.net.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class FTBQuestsNetHandlerImpl {
	public static void writeItemType(FriendlyByteBuf buffer, ItemStack stack) {
		buffer.writeItem(stack);
	}

	public static ItemStack readItemType(FriendlyByteBuf buffer) {
		return buffer.readItem();
	}
}
