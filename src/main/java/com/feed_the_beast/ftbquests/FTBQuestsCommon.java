package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;

public class FTBQuestsCommon
{
	public QuestList getQuestList(boolean clientSide)
	{
		return ServerQuestList.INSTANCE;
	}
}