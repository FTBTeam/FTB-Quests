package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class ClearFileCacheEvent {
	public static final Event<Consumer<ClearFileCacheEvent>> EVENT = EventFactory.createConsumerLoop(ClearFileCacheEvent.class);
	private final QuestFile file;

	public ClearFileCacheEvent(QuestFile f) {
		file = f;
	}

	public QuestFile getFile() {
		return file;
	}
}