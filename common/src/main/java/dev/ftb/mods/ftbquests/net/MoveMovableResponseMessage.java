package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

public record MoveMovableResponseMessage(long id, long chapterId, double x, double y) implements CustomPacketPayload {
	public static final Type<MoveMovableResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_movable_response_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveMovableResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveMovableResponseMessage::id,
			ByteBufCodecs.VAR_LONG, MoveMovableResponseMessage::chapterId,
			ByteBufCodecs.DOUBLE, MoveMovableResponseMessage::x,
			ByteBufCodecs.DOUBLE, MoveMovableResponseMessage::y,
			MoveMovableResponseMessage::new
	);

	@Override
	public Type<MoveMovableResponseMessage> type() {
		return TYPE;
	}

	public static void handle(MoveMovableResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.moveQuest(message.id, message.chapterId, message.x, message.y));
	}
}
