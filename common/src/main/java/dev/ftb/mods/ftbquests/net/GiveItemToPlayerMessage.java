package dev.ftb.mods.ftbquests.net;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record GiveItemToPlayerMessage(ItemStack stack) implements CustomPacketPayload {
	public static final Type<GiveItemToPlayerMessage> TYPE = new Type<>(FTBQuestsAPI.rl("give_item_to_player"));

	public static final StreamCodec<RegistryFriendlyByteBuf, GiveItemToPlayerMessage> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, GiveItemToPlayerMessage::stack,
			GiveItemToPlayerMessage::new
	);

	@Override
	public Type<GiveItemToPlayerMessage> type() {
		return TYPE;
	}

	public static void handle(GiveItemToPlayerMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			if (PermissionsHelper.hasEditorPermission(player, false)) {
				ItemStackHooks.giveItem(player, message.stack);
				player.displayClientMessage(Component.translatable("ftbquests.task.gave_item", message.stack.toString()), false);
			}
		});
	}
}
