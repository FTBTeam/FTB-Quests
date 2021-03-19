package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.server.ServerEventJS;
import dev.latvian.kubejs.server.ServerJS;

/**
 * @author LatvianModder
 */
public class QuestObjectCompletedEventJS extends ServerEventJS {
	public final ObjectCompletedEvent event;

	@Override
	public ServerJS getServer() {
		return ServerJS.instance;
	}

	public QuestObjectCompletedEventJS(ObjectCompletedEvent e) {
		event = e;
	}

	public PlayerData getData() {
		return event.getData();
	}

	public QuestObject getObject() {
		return event.getObject();
	}

	public EntityArrayList getNotifiedPlayers() {
		return ServerJS.instance.getOverworld().createEntityList(event.getNotifiedPlayers());
	}

	public EntityArrayList getOnlineMembers() {
		return ServerJS.instance.getOverworld().createEntityList(getData().getOnlineMembers());
	}
}