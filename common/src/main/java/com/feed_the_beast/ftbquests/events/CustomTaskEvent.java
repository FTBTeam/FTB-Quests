package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import me.shedaniel.architectury.ForgeEventCancellable;
import me.shedaniel.architectury.event.Actor;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
@ForgeEventCancellable
public class CustomTaskEvent extends FTBQuestsEvent
{
    public static final Event<Actor<CustomTaskEvent>> EVENT = EventFactory.createActorLoop();
	private final CustomTask task;

	public CustomTaskEvent(CustomTask t)
	{
		task = t;
	}

	public CustomTask getTask()
	{
		return task;
	}
}