package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveChapterGroupResponseMessage(long id, boolean movingUp) implements CustomPacketPayload {
	public static final Type<MoveChapterGroupResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_chapter_group_response_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveChapterGroupResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveChapterGroupResponseMessage::id,
			ByteBufCodecs.BOOL, MoveChapterGroupResponseMessage::movingUp,
			MoveChapterGroupResponseMessage::new
	);

	@Override
	public Type<MoveChapterGroupResponseMessage> type() {
		return TYPE;
	}

	public static void handle(MoveChapterGroupResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.moveChapterGroup(message.id, message.movingUp));
	}
}
