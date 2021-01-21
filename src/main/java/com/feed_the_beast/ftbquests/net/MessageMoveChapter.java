package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveChapter extends MessageBase
{
	private final int id;
	private final boolean left;

	public MessageMoveChapter(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
		left = buffer.readBoolean();
	}

	public MessageMoveChapter(int i, boolean l)
	{
		id = i;
		left = l;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
		buffer.writeBoolean(left);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = chapter.file.chapters.indexOf(chapter);

				if (index != -1 && left ? (index > 0) : (index < ServerQuestFile.INSTANCE.chapters.size() - 1))
				{
					chapter.file.chapters.remove(index);
					chapter.file.chapters.add(left ? index - 1 : index + 1, chapter);
					chapter.file.refreshIDMap();
					chapter.file.clearCachedData();
					new MessageMoveChapterResponse(id, left).sendToAll();
					chapter.file.updateChapterIndices();
					chapter.file.save();
				}
			}
		}
	}
}