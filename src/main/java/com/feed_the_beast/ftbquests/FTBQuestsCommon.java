package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;

public class FTBQuestsCommon
{
	public void preInit()
	{
	}

	public QuestFile getQuestList(boolean clientSide)
	{
		return ServerQuestFile.INSTANCE;
	}
}