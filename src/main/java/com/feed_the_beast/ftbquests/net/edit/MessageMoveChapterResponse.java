package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterResponse extends MessageToClient
{
	private int id;
	private boolean up;

	public MessageMoveChapterResponse()
	{
	}

	public MessageMoveChapterResponse(int i, boolean u)
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
		data.writeInt(id);
		data.writeBoolean(up);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		up = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			QuestChapter chapter = ClientQuestList.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = ClientQuestList.INSTANCE.chapters.indexOf(chapter);

				if (index != -1 && up ? (index > 0) : (index < ClientQuestList.INSTANCE.chapters.size() - 1))
				{
					ClientQuestList.INSTANCE.chapters.remove(index);
					ClientQuestList.INSTANCE.chapters.add(up ? index - 1 : index + 1, chapter);
					ClientQuestList.INSTANCE.refreshGui(ClientQuestList.INSTANCE);
				}
			}
		}
	}
}