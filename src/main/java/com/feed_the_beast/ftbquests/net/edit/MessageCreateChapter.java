package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * @author LatvianModder
 */
public class MessageCreateChapter extends MessageToServer
{
	private int index;
	private String title;

	public MessageCreateChapter()
	{
	}

	public MessageCreateChapter(int idx, String t)
	{
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
		data.writeInt(index);
		data.writeString(title);
	}

	@Override
	public void readData(DataIn data)
	{
		index = data.readInt();
		title = data.readString();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT))
		{
			QuestChapter chapter = new QuestChapter(ServerQuestList.INSTANCE, ServerQuestList.INSTANCE.requestID());
			chapter.title = title;
			ServerQuestList.INSTANCE.objectMap.put(chapter.id, chapter);
			ServerQuestList.INSTANCE.chapters.add(index, chapter);
			new MessageCreateChapterResponse(chapter.id, index, title).sendToAll();
			ServerQuestList.INSTANCE.save();
		}
	}
}