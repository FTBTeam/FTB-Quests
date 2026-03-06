package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;

public record MoveChapterGroupMessage(long id, boolean movingUp) implements CustomPacketPayload {
	public static final Type<MoveChapterGroupMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_chapter_group_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveChapterGroupMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveChapterGroupMessage::id,
			ByteBufCodecs.BOOL, MoveChapterGroupMessage::movingUp,
			MoveChapterGroupMessage::new
	);

	@Override
	public Type<MoveChapterGroupMessage> type() {
		return TYPE;
	}

	public static void handle(MoveChapterGroupMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				ServerQuestFile.getInstance().moveChapterGroup(message.id, message.movingUp);
			}
		});
	}
}
