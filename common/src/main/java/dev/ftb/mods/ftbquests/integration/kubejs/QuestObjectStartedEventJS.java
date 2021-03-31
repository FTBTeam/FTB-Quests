package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.server.ServerEventJS;
import dev.latvian.kubejs.server.ServerJS;

/**
 * @author LatvianModder
 */
public class QuestObjectStartedEventJS extends ServerEventJS {
	public final ObjectStartedEvent<?> event;

	@Override
	public ServerJS getServer() {
		return ServerJS.instance;
	}

	public QuestObjectStartedEventJS(ObjectStartedEvent<?> e) {
		event = e;
	}

	public TeamData getData() {
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