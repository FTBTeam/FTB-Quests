package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;

public class FTBQuestsClient extends FTBQuestsCommon
{
	@Override
	public QuestFile getQuestList(boolean clientSide)
	{
		return clientSide ? ClientQuestFile.INSTANCE : ServerQuestFile.INSTANCE;
	}
}