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
public class MessageCreateChapterResponse extends MessageToClient
{
	private int id, index;
	private String title;

	public MessageCreateChapterResponse()
	{
	}

	public MessageCreateChapterResponse(int i, int idx, String t)
	{
		id = i;
		index = idx;
		title = t;
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
		data.writeInt(index);
		data.writeString(title);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		index = data.readInt();
		title = data.readString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			QuestChapter chapter = new QuestChapter(ClientQuestList.INSTANCE, id);
			chapter.title = title;
			ClientQuestList.INSTANCE.objectMap.put(chapter.id, chapter);
			ClientQuestList.INSTANCE.chapters.add(index, chapter);
			ClientQuestList.INSTANCE.refreshGui(ClientQuestList.INSTANCE);
		}
	}
}