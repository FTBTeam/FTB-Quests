package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.task.TaskData;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class TaskStartedEvent
{
	public static final Event<Consumer<TaskStartedEvent>> EVENT = EventFactory.createConsumerLoop(TaskStartedEvent.class);
	private final TaskData data;

	public TaskStartedEvent(TaskData d)
	{
		data = d;
	}

	public TaskData getTaskData()
	{
		return data;
	}
}