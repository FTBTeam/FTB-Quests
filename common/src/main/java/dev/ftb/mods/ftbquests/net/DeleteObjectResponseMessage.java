package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

public record DeleteObjectResponseMessage(long id) implements CustomPacketPayload {
	public static final Type<DeleteObjectResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("delete_object_response_message"));

	public static final StreamCodec<FriendlyByteBuf, DeleteObjectResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, DeleteObjectResponseMessage::id,
			DeleteObjectResponseMessage::new
	);

	@Override
	public Type<DeleteObjectResponseMessage> type() {
		return TYPE;
	}

	public static void handle(DeleteObjectResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.deleteObject(message.id));}
}
