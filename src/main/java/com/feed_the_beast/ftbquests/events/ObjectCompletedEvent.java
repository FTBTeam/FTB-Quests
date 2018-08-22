package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestObject;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent extends FTBQuestsEvent
{
	private final IProgressData team;
	private final QuestObject object;

	public ObjectCompletedEvent(IProgressData t, QuestObject o)
	{
		team = t;
		object = o;
	}

	public IProgressData getTeam()
	{
		return team;
	}

	public QuestObject getObject()
	{
		return object;
	}
}