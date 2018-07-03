package com.feed_the_beast.ftbquests.client;

import com.feed_the_beast.ftbquests.FTBQuestsCommon;
import com.feed_the_beast.ftbquests.gui.ClientQuestList;
import com.feed_the_beast.ftbquests.quest.IProgressData;

import javax.annotation.Nullable;

public class FTBQuestsClient extends FTBQuestsCommon
{
	@Override
	@Nullable
	public IProgressData getOwner(String owner, boolean clientSide)
	{
		return clientSide ? (ClientQuestList.exists() && ClientQuestList.INSTANCE.getTeamID().equals(owner) ? ClientQuestList.INSTANCE : null) : super.getOwner(owner, false);
	}
}