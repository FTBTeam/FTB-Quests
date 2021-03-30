package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.QuestObject;
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