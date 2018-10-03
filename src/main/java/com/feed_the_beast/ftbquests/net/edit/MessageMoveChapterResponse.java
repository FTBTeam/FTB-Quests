package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterResponse extends MessageToClient
{
	private String id;
	private boolean up;

	public MessageMoveChapterResponse()
	{
	}

	public MessageMoveChapterResponse(String i, boolean u)
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
		data.writeString(id);
		data.writeBoolean(up);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readString();
		up = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.INSTANCE != null)
		{
			QuestChapter chapter = ClientQuestFile.INSTANCE.getChapter(id);

			if (chapter != null)
			{
				int index = ClientQuestFile.INSTANCE.chapters.indexOf(chapter);

				if (index != -1 && up ? (index > 0) : (index < ClientQuestFile.INSTANCE.chapters.size() - 1))
				{
					ClientQuestFile.INSTANCE.chapters.remove(index);
					ClientQuestFile.INSTANCE.chapters.add(up ? index - 1 : index + 1, chapter);
					ClientQuestFile.INSTANCE.refreshIDMap();

					GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

					if (gui != null)
					{
						gui.chapterPanel.refreshWidgets();
						gui.chapterPanel.alignWidgets();
					}
				}
			}
		}
	}
}