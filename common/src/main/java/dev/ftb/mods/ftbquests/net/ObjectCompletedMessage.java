package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Date;
import java.util.UUID;

public record ObjectCompletedMessage(UUID teamId, long id) implements CustomPacketPayload {
	public static final Type<ObjectCompletedMessage> TYPE = new Type<>(FTBQuestsAPI.id("object_completed_message"));

	public static final StreamCodec<FriendlyByteBuf, ObjectCompletedMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ObjectCompletedMessage::teamId,
			ByteBufCodecs.VAR_LONG, ObjectCompletedMessage::id,
			ObjectCompletedMessage::new
	);

	@Override
	public Type<ObjectCompletedMessage> type() {
		return TYPE;
	}

	public static void handle(ObjectCompletedMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.objectCompleted(message.teamId, message.id, new Date()));
	}
}
