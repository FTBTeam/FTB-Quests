package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DisplayCompletionToastMessage(long id) implements CustomPacketPayload {
	public static final Type<DisplayCompletionToastMessage> TYPE = new Type<>(FTBQuestsAPI.rl("display_completion_toast_message"));

	public static final StreamCodec<FriendlyByteBuf, DisplayCompletionToastMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, DisplayCompletionToastMessage::id,
			DisplayCompletionToastMessage::new
	);

	@Override
	public Type<DisplayCompletionToastMessage> type() {
		return TYPE;
	}

	public static void handle(DisplayCompletionToastMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.notifyPlayerOfCompletion(message.id));
	}
}