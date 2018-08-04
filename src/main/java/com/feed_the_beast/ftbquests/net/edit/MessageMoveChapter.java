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
	private short id;
	private boolean up;

	public MessageMoveChapter()
	{
	}

	public MessageMoveChapter(short i, boolean u)
	{
		id = i;
		up = u;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(id);
		data.writeBoolean(up);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readShort();
		up = data.readBoolean();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (id != 0 && FTBQuests.canEdit(player))
		{
			QuestChapter chapter = ServerQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = ServerQuestFile.INSTANCE.chapters.indexOf(chapter);

				if (index != -1 && up ? (index > 0) : (index < ServerQuestFile.INSTANCE.chapters.size() - 1))
				{
					ServerQuestFile.INSTANCE.chapters.remove(index);
					ServerQuestFile.INSTANCE.chapters.add(up ? index - 1 : index + 1, chapter);

					for (int i = 0; i < ServerQuestFile.INSTANCE.chapters.size(); i++)
					{
						ServerQuestFile.INSTANCE.chapters.get(i).index = i;
					}

					new MessageMoveChapterResponse(id, up).sendToAll();
					ServerQuestFile.INSTANCE.save();
				}
			}
		}
	}
}