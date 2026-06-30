package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveChapterAcrossGroup;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeChapterGroupMessage(long chapterId, long groupId) implements CustomPacketPayload {
	public static final Type<ChangeChapterGroupMessage> TYPE = new Type<>(FTBQuestsAPI.rl("change_chapter_group_message"));

	public static final StreamCodec<FriendlyByteBuf, ChangeChapterGroupMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ChangeChapterGroupMessage::chapterId,
			ByteBufCodecs.VAR_LONG, ChangeChapterGroupMessage::groupId,
			ChangeChapterGroupMessage::new
	);

	@Override
	public Type<ChangeChapterGroupMessage> type() {
		return TYPE;
	}

	public static void handle(ChangeChapterGroupMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> ServerQuestFile.getInstance().ifPresent(sqf -> {
            if (NetUtils.canEdit(context) && sqf.getChapter(message.chapterId) instanceof Chapter chapter) {
                sqf.getHistoryStack().addAndApply(sqf, new MoveChapterAcrossGroup(message.chapterId, chapter.getGroup().getId(), message.groupId()));
            }
		}));
	}
}