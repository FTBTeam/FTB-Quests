package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ChapterGroup;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterGroup extends MessageBase {
	private final long id;
	private final boolean up;

	public MessageMoveChapterGroup(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MessageMoveChapterGroup(long i, boolean u) {
		id = i;
		up = u;
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
					new MessageMoveChapterGroupResponse(id, up).sendToAll();
					group.file.save();
				}
			}
		}
	}
}