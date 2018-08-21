package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.ProgressingQuestObject;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent extends FTBQuestsEvent
{
	private final IProgressData team;
	private final ProgressingQuestObject object;

	public ObjectCompletedEvent(IProgressData t, ProgressingQuestObject o)
	{
		team = t;
		object = o;
	}

	public IProgressData getTeam()
	{
		return team;
	}

	public ProgressingQuestObject getObject()
	{
		return object;
	}
}