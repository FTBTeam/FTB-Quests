package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.QuestObject;

/**
 * @author LatvianModder
 */
public class ObjectCompletedEvent extends FTBQuestsEvent
{
	private final ITeamData team;
	private final QuestObject object;

	public ObjectCompletedEvent(ITeamData t, QuestObject o)
	{
		team = t;
		object = o;
	}

	public ITeamData getTeam()
	{
		return team;
	}

	public QuestObject getObject()
	{
		return object;
	}
}