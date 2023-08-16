package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;

public class MoveChapterMessage extends BaseC2SMessage {
	private final long id;
	private final boolean up;

	public MoveChapterMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MoveChapterMessage(long i, boolean u) {
		id = i;
		up = u;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_CHAPTER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null) {
				int index = chapter.group.chapters.indexOf(chapter);

				if (index != -1 && up ? (index > 0) : (index < chapter.group.chapters.size() - 1)) {
					chapter.group.chapters.remove(index);
					chapter.group.chapters.add(up ? index - 1 : index + 1, chapter);
					chapter.file.clearCachedData();
					new MoveChapterResponseMessage(id, up).sendToAll(context.getPlayer().getServer());
					chapter.file.save();
				}
			}
		}
	}
}
