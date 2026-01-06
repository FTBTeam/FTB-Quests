package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ObjectStartedResetMessage(UUID teamId, long id) implements CustomPacketPayload {
	public static final Type<ObjectStartedResetMessage> TYPE = new Type<>(FTBQuestsAPI.id("object_started_reset_message"));

	public static final StreamCodec<FriendlyByteBuf, ObjectStartedResetMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ObjectStartedResetMessage::teamId,
			ByteBufCodecs.VAR_LONG, ObjectStartedResetMessage::id,
			ObjectStartedResetMessage::new
	);

	@Override
	public Type<ObjectStartedResetMessage> type() {
		return TYPE;
	}

	public static void handle(ObjectStartedResetMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.objectStarted(message.teamId, message.id, null));
	}
}
