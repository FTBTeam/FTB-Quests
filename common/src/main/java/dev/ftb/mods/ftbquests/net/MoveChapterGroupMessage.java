package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;

public class MoveChapterGroupMessage extends BaseC2SMessage {
	private final long id;
	private final boolean up;

	public MoveChapterGroupMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MoveChapterGroupMessage(long i, boolean u) {
		id = i;
		up = u;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_CHAPTER_GROUP;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (NetUtils.canEdit(context)) {
			ChapterGroup group = ServerQuestFile.INSTANCE.getChapterGroup(id);

			if (!group.isDefaultGroup()) {
				int index = group.file.chapterGroups.indexOf(group);

				if (index != -1 && up ? (index > 1) : (index < group.file.chapterGroups.size() - 1)) {
					group.file.chapterGroups.remove(index);
					group.file.chapterGroups.add(up ? index - 1 : index + 1, group);
					group.file.clearCachedData();
					new MoveChapterGroupResponseMessage(id, up).sendToAll(context.getPlayer().getServer());
					group.file.save();
				}
			}
		}
	}
}
