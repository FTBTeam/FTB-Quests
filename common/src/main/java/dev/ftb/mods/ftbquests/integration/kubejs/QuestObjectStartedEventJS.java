package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.latvian.kubejs.player.EntityArrayList;
import dev.latvian.kubejs.player.ServerPlayerJS;
import dev.latvian.kubejs.server.ServerEventJS;
import dev.latvian.kubejs.server.ServerJS;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class QuestObjectStartedEventJS extends ServerEventJS {
	public final ObjectStartedEvent<?> event;
	private final FTBQuestsKubeJSTeamDataWrapper wrapper;

	@Override
	public ServerJS getServer() {
		return ServerJS.instance;
	}

	public QuestObjectStartedEventJS(ObjectStartedEvent<?> e) {
		event = e;
		wrapper = new FTBQuestsKubeJSTeamDataWrapper(event.getData());
	}

	public FTBQuestsKubeJSTeamDataWrapper getData() {
		return wrapper;
	}

	public QuestObject getObject() {
		return event.getObject();
	}

	public EntityArrayList getNotifiedPlayers() {
		return ServerJS.instance.getOverworld().createEntityList(event.getNotifiedPlayers());
	}

	public EntityArrayList getOnlineMembers() {
		return getData().getOnlineMembers();
	}

	@Nullable
	public ServerPlayerJS getPlayer() {
		return TeamData.currentPlayer == null ? null : ServerJS.instance.getPlayer(TeamData.currentPlayer);
	}
}