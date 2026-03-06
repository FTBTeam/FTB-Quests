package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

public record NotifyCompletionMessage(long id) implements CustomPacketPayload {
	public static final Type<NotifyCompletionMessage> TYPE = new Type<>(FTBQuestsAPI.id("notify_completion_message"));

	public static final StreamCodec<FriendlyByteBuf, NotifyCompletionMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, NotifyCompletionMessage::id,
			NotifyCompletionMessage::new
	);

	@Override
	public Type<NotifyCompletionMessage> type() {
		return TYPE;
	}

	public static void handle(NotifyCompletionMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.notifyPlayerOfCompletion(message.id));
	}
}
