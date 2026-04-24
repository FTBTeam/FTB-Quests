package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record GiveItemToPlayerMessage(ItemStack stack) implements CustomPacketPayload {
	public static final Type<GiveItemToPlayerMessage> TYPE = new Type<>(FTBQuestsAPI.id("give_item_to_player"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GiveItemToPlayerMessage> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, GiveItemToPlayerMessage::stack,
			GiveItemToPlayerMessage::new
	);

	@Override
	public Type<GiveItemToPlayerMessage> type() {
		return TYPE;
	}

	public static void handle(GiveItemToPlayerMessage message, PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.player();
		if (PermissionsHelper.hasEditorPermission(player, false)) {
			player.getInventory().placeItemBackInInventory(message.stack.copy());
			player.sendSystemMessage(Component.translatable("ftbquests.task.gave_item", message.stack.toString()));
		}
	}
}
