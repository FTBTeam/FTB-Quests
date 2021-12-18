package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.QuestFile;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public interface ClearFileCacheEvent {
	Event<Consumer<QuestFile>> EVENT = EventFactory.createConsumerLoop(QuestFile.class);
}