package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeChapterGroupMessage(long chapterId, long groupId) implements CustomPacketPayload {
	public static final Type<ChangeChapterGroupMessage> TYPE = new Type<>(FTBQuestsAPI.id("change_chapter_group_message"));

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
		if (NetUtils.canEdit(context)) {
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(message.chapterId);

			if (chapter != null) {
				ChapterGroup group = ServerQuestFile.INSTANCE.getChapterGroup(message.groupId);
				if (chapter.getGroup() != group) {
					chapter.getGroup().removeChapter(chapter);
					group.addChapter(chapter);
					chapter.file.clearCachedData();
					chapter.file.markDirty();
					NetworkHelper.sendToAll(context.getPlayer().level().getServer(),
							new ChangeChapterGroupResponseMessage(message.chapterId, message.groupId));
				}
			}
		}
	}
}
