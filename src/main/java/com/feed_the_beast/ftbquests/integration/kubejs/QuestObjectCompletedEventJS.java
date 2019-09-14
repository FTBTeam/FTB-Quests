package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import dev.latvian.kubejs.documentation.Ignore;
import dev.latvian.kubejs.documentation.Info;
import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.server.ServerJS;

/**
 * @author LatvianModder
 */
@Info("Event that gets fired when an object is completed. It can be a file, quest, chapter, task")
public class QuestObjectCompletedEventJS extends EventJS
{
	@Ignore
	public final ObjectCompletedEvent event;

	public QuestObjectCompletedEventJS(ObjectCompletedEvent e)
	{
		event = e;
	}

	public QuestData getData()
	{
		return event.getData();
	}

	public QuestObject getObject()
	{
		return event.getObject();
	}

	@Info("List of notified players. It isn't always the list of online members of that team, for example, this list is empty when invisible quest was completed")
	public EntityArrayList getNotifiedPlayers()
	{
		return ServerJS.instance.getOverworld().createEntityList(event.getNotifiedPlayers());
	}

	@Info("List of all online team members")
	public EntityArrayList getOnlineMembers()
	{
		return ServerJS.instance.getOverworld().createEntityList(getData().getOnlineMembers());
	}
}