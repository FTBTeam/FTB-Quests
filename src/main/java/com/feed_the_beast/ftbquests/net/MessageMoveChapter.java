package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageMoveChapter extends MessageBase
{
	private final int id;
	private final boolean left;

	public MessageMoveChapter(PacketBuffer buffer)
	{
		id = buffer.readInt();
		left = buffer.readBoolean();
	}

	public MessageMoveChapter(int i, boolean l)
	{
		id = i;
		left = l;
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeInt(id);
		buffer.writeBoolean(left);
	}

	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			Chapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = ServerQuestFile.INSTANCE.chapters.indexOf(chapter);

				if (index != -1 && left ? (index > 0) : (index < ServerQuestFile.INSTANCE.chapters.size() - 1))
				{
					ServerQuestFile.INSTANCE.chapters.remove(index);
					ServerQuestFile.INSTANCE.chapters.add(left ? index - 1 : index + 1, chapter);
					ServerQuestFile.INSTANCE.refreshIDMap();
					ServerQuestFile.INSTANCE.clearCachedData();
					new MessageMoveChapterResponse(id, left).sendToAll();
					ServerQuestFile.INSTANCE.save();
				}
			}
		}
	}
}