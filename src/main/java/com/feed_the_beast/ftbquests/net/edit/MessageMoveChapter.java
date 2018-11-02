package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageMoveChapter extends MessageToServer
{
	private int id;
	private boolean left;

	public MessageMoveChapter()
	{
	}

	public MessageMoveChapter(int i, boolean l)
	{
		id = i;
		left = l;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
		data.writeBoolean(left);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		left = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestChapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

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