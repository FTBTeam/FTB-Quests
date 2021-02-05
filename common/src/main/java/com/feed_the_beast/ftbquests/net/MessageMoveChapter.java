package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageMoveChapter extends MessageBase
{
	private final long id;
	private final boolean left;

	public MessageMoveChapter(FriendlyByteBuf buffer)
	{
		id = buffer.readLong();
		left = buffer.readBoolean();
	}

	public MessageMoveChapter(long i, boolean l)
	{
		id = i;
		left = l;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeLong(id);
		buffer.writeBoolean(left);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		if (NetUtils.canEdit(context))
		{
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = chapter.group.chapters.indexOf(chapter);

				if (index != -1 && left ? (index > 0) : (index < chapter.group.chapters.size() - 1))
				{
					chapter.group.chapters.remove(index);
					chapter.group.chapters.add(left ? index - 1 : index + 1, chapter);
					chapter.file.clearCachedData();
					new MessageMoveChapterResponse(id, left).sendToAll();
					chapter.file.save();
				}
			}
		}
	}
}