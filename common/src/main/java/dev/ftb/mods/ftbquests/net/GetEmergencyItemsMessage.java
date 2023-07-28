package dev.ftb.mods.ftbquests.net;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GetEmergencyItemsMessage extends BaseC2SMessage {
	GetEmergencyItemsMessage(FriendlyByteBuf buffer) {
	}

	public GetEmergencyItemsMessage() {
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.GET_EMERGENCY_ITEMS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		ServerQuestFile.INSTANCE.getEmergencyItems()
				.forEach(stack -> ItemStackHooks.giveItem(player, stack.copy()));
	}
}