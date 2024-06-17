package dev.ftb.mods.ftbquests.net;

import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record GetEmergencyItemsMessage() implements CustomPacketPayload {
	public static final Type<GetEmergencyItemsMessage> TYPE = new Type<>(FTBQuestsAPI.rl("get_emergency_items_message"));

	public static final GetEmergencyItemsMessage INSTANCE = new GetEmergencyItemsMessage();

	public static final StreamCodec<FriendlyByteBuf, GetEmergencyItemsMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<GetEmergencyItemsMessage> type() {
		return TYPE;
	}

	public static void handle(GetEmergencyItemsMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			ServerQuestFile.INSTANCE.getEmergencyItems()
					.forEach(stack -> ItemStackHooks.giveItem(player, stack.copy()));
		});
	}
}