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

public record ObjectStartedMessage(UUID teamId, long id) implements CustomPacketPayload {
	public static final Type<ObjectStartedMessage> TYPE = new Type<>(FTBQuestsAPI.id("object_started_message"));

	public static final StreamCodec<FriendlyByteBuf, ObjectStartedMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ObjectStartedMessage::teamId,
			ByteBufCodecs.VAR_LONG, ObjectStartedMessage::id,
			ObjectStartedMessage::new
	);


	@Override
	public Type<ObjectStartedMessage> type() {
		return TYPE;
	}

	public static void handle(ObjectStartedMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.objectStarted(message.teamId, message.id, new Date()));
	}
}
