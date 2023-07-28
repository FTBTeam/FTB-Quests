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
	private final boolean movingUp;

	public MoveChapterMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		movingUp = buffer.readBoolean();
	}

	public MoveChapterMessage(long id, boolean movingUp) {
		this.id = id;
		this.movingUp = movingUp;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_CHAPTER;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(movingUp);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null && chapter.getGroup().moveChapterWithinGroup(chapter, movingUp)) {
				chapter.file.clearCachedData();
				new MoveChapterResponseMessage(id, movingUp).sendToAll(ServerQuestFile.INSTANCE.server);
				chapter.file.markDirty();
			}
		}
	}
}