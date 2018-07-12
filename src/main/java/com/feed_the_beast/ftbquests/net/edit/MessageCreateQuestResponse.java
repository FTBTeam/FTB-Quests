package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageCreateQuestResponse extends MessageToClient
{
	private int id, chapter;
	private String title;
	private int x, y;

	public MessageCreateQuestResponse()
	{
	}

	public MessageCreateQuestResponse(int i, int c, String t, int _x, int _y)
	{
		id = i;
		chapter = c;
		title = t;
		x = _x;
		y = _y;
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
		data.writeInt(chapter);
		data.writeString(title);
		data.writeInt(x);
		data.writeInt(y);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
		chapter = data.readInt();
		title = data.readString();
		x = data.readInt();
		y = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestList.INSTANCE != null)
		{
			QuestChapter c = ClientQuestList.INSTANCE.getChapter(chapter);

			if (c != null)
			{
				Quest quest = new Quest(c, id);
				quest.title = title;
				quest.x = x;
				quest.y = y;
				ClientQuestList.INSTANCE.objectMap.put(quest.id, quest);
				c.quests.add(quest);
				ClientQuestList.INSTANCE.refreshGui(ClientQuestList.INSTANCE);
			}
		}
	}
}