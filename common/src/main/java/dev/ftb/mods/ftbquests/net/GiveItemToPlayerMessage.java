package dev.ftb.mods.ftbquests.net;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GiveItemToPlayerMessage extends BaseC2SMessage {
	private final ItemStack stack;

	public GiveItemToPlayerMessage(ItemStack stack) {
		this.stack = stack;
	}

	public GiveItemToPlayerMessage(FriendlyByteBuf buf) {
		stack = buf.readItem();
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.GIVE_ITEM_TO_PLAYER;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeItem(stack);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			if (PermissionsHelper.hasEditorPermission(player, false)) {
				ItemStackHooks.giveItem(player, stack);
				player.displayClientMessage(Component.translatable("ftbquests.task.gave_item", stack.toString()), false);
			}
		});
	}
}
