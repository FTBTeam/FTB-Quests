package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.latvian.mods.kubejs.player.EntityArrayList;
import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class QuestObjectCompletedEventJS extends ServerEventJS {
	public final ObjectCompletedEvent<?> event;
	private final FTBQuestsKubeJSTeamDataWrapper wrapper;

	public QuestObjectCompletedEventJS(ObjectCompletedEvent<?> e) {
		super(UtilsJS.staticServer);
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
		return new EntityArrayList(server.overworld(), event.getNotifiedPlayers());
	}

	public EntityArrayList getOnlineMembers() {
		return getData().getOnlineMembers();
	}

	@Nullable
	public ServerPlayer getPlayer() {
		return event.getData().file instanceof ServerQuestFile file ? file.currentPlayer : null;
	}
}
