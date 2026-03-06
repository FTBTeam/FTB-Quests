package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;

public record MoveChapterMessage(long id, boolean movingUp) implements CustomPacketPayload {
	public static final Type<MoveChapterMessage> TYPE = new Type<>(FTBQuestsAPI.id("move_chapter_message"));

	public static final StreamCodec<FriendlyByteBuf, MoveChapterMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, MoveChapterMessage::id,
			ByteBufCodecs.BOOL, MoveChapterMessage::movingUp,
			MoveChapterMessage::new
	);

	@Override
	public Type<MoveChapterMessage> type() {
		return TYPE;
	}

	public static void handle(MoveChapterMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			if (NetUtils.canEdit(context)) {
				Chapter chapter = ServerQuestFile.getInstance().getChapter(message.id);

				if (chapter != null && chapter.getGroup().moveChapterWithinGroup(chapter, message.movingUp)) {
					chapter.file.clearCachedData();
					NetworkHelper.sendToAll(ServerQuestFile.getInstance().server, new MoveChapterResponseMessage(message.id, message.movingUp));
					chapter.file.markDirty();
				}
			}
		});
	}
}
