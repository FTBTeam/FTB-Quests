package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageChangeChapterGroup extends MessageBase {
	private final long id;
	private final long group;

	public MessageChangeChapterGroup(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		group = buffer.readLong();
	}

	public MessageChangeChapterGroup(long i, long g) {
		id = i;
		group = g;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(group);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null) {
				ChapterGroup g = ServerQuestFile.INSTANCE.getChapterGroup(group);

				if (chapter.group != g) {
					chapter.group.chapters.remove(chapter);
					chapter.group = g;
					g.chapters.add(chapter);
					chapter.file.clearCachedData();
					chapter.file.save();
					new MessageChangeChapterGroupResponse(id, group).sendToAll();
				}
			}
		}
	}
}