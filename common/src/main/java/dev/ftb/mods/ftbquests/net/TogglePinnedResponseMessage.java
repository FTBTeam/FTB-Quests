package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TogglePinnedResponseMessage(long id, boolean pinned) implements CustomPacketPayload {
	public static final Type<TogglePinnedResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("toggle_pinned_response_message"));

	public static final StreamCodec<FriendlyByteBuf, TogglePinnedResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, TogglePinnedResponseMessage::id,
			ByteBufCodecs.BOOL, TogglePinnedResponseMessage::pinned,
			TogglePinnedResponseMessage::new
	);

	@Override
	public Type<TogglePinnedResponseMessage> type() {
		return TYPE;
	}

	public static void handle(TogglePinnedResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.togglePinned(message.id, message.pinned));
	}
}
