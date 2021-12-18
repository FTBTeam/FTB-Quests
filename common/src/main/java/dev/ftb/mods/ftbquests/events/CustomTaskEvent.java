package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;

/**
 * @author LatvianModder
 */
public class CustomTaskEvent {
	public static final Event<EventActor<CustomTaskEvent>> EVENT = EventFactory.createEventActorLoop();
	private final CustomTask task;

	public CustomTaskEvent(CustomTask t) {
		task = t;
	}

	public CustomTask getTask() {
		return task;
	}
}