package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public interface ClearFileCacheEvent {
	Event<Consumer<BaseQuestFile>> EVENT = EventFactory.createConsumerLoop(BaseQuestFile.class);
}