package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ClearFileCacheEvent extends FTBQuestsEvent
{
    public static final Event<Consumer<ClearFileCacheEvent>> EVENT = EventFactory.createConsumerLoop(ClearFileCacheEvent.class);
	private final QuestFile file;

	public ClearFileCacheEvent(QuestFile f)
	{
		file = f;
	}

	public QuestFile getFile()
	{
		return file;
	}
}