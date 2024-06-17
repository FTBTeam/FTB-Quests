package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeChapterGroupResponseMessage(long id, long group) implements CustomPacketPayload {
	public static final Type<ChangeChapterGroupResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("change_chapter_group_response_message"));

	public static final StreamCodec<FriendlyByteBuf, ChangeChapterGroupResponseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ChangeChapterGroupResponseMessage::id,
			ByteBufCodecs.VAR_LONG, ChangeChapterGroupResponseMessage::group,
			ChangeChapterGroupResponseMessage::new
	);

	@Override
	public Type<ChangeChapterGroupResponseMessage> type() {
		return TYPE;
	}

	public static void handle(ChangeChapterGroupResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.changeChapterGroup(message.id, message.group));
	}
}