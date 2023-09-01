package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;

public class ChangeChapterGroupMessage extends BaseC2SMessage {
	private final long chapterId;
	private final long groupId;

	public ChangeChapterGroupMessage(FriendlyByteBuf buffer) {
		chapterId = buffer.readLong();
		groupId = buffer.readLong();
	}

	public ChangeChapterGroupMessage(long chapterId, long groupId) {
		this.chapterId = chapterId;
		this.groupId = groupId;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CHANGE_CHAPTER_GROUP;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(chapterId);
		buffer.writeLong(groupId);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(chapterId);

			if (chapter != null) {
				ChapterGroup group = ServerQuestFile.INSTANCE.getChapterGroup(groupId);
				if (chapter.getGroup() != group) {
					chapter.getGroup().removeChapter(chapter);
					group.addChapter(chapter);
					chapter.file.clearCachedData();
					chapter.file.markDirty();
					new ChangeChapterGroupResponseMessage(chapterId, groupId).sendToAll(context.getPlayer().getServer());
				}
			}
		}
	}
}