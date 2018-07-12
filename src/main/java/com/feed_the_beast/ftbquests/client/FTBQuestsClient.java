package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.QuestList;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;

public class FTBQuestsClient extends FTBQuestsCommon
{
	@Override
	public QuestList getQuestList(boolean clientSide)
	{
		return clientSide ? ClientQuestList.INSTANCE : ServerQuestList.INSTANCE;
	}
}