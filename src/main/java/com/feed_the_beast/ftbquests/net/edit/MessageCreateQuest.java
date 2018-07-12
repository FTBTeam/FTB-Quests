package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * @author LatvianModder
 */
public class MessageCreateQuest extends MessageToServer
{
	private int chapter;
	private String title;
	private int x, y;

	public MessageCreateQuest()
	{
	}

	public MessageCreateQuest(int c, String t, int _x, int _y)
	{
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
		data.writeInt(chapter);
		data.writeString(title);
		data.writeInt(x);
		data.writeInt(y);
	}

	@Override
	public void readData(DataIn data)
	{
		chapter = data.readInt();
		title = data.readString();
		x = data.readInt();
		y = data.readInt();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (PermissionAPI.hasPermission(player, FTBQuests.PERM_EDIT))
		{
			QuestChapter c = ServerQuestList.INSTANCE.getChapter(chapter);

			if (c != null)
			{
				Quest quest = new Quest(c, ServerQuestList.INSTANCE.requestID());
				quest.title = title;
				quest.x = x;
				quest.y = y;
				ServerQuestList.INSTANCE.objectMap.put(quest.id, quest);
				c.quests.add(quest);
				new MessageCreateQuestResponse(quest.id, chapter, title, x, y).sendToAll();
				ServerQuestList.INSTANCE.save();
			}
		}
	}
}