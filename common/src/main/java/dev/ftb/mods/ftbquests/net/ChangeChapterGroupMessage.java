package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseC2SMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class ChangeChapterGroupMessage extends BaseC2SMessage {
	private final long id;
	private final long group;

	public ChangeChapterGroupMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		group = buffer.readLong();
	}

	public ChangeChapterGroupMessage(long i, long g) {
		id = i;
		group = g;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CHANGE_CHAPTER_GROUP;
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
					new ChangeChapterGroupResponseMessage(id, group).sendToAll(context.getPlayer().getServer());
				}
			}
		}
	}
}