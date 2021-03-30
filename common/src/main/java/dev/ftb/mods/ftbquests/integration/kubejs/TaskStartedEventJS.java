package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.ftb.mods.ftbquests.events.TaskStartedEvent;
import dev.ftb.mods.ftbquests.quest.task.TaskData;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
public class TaskStartedEventJS extends EventJS {
	public final transient TaskStartedEvent event;

	public TaskStartedEventJS(TaskStartedEvent e) {
		event = e;
	}

	public TaskData getTaskData() {
		return event.getTaskData();
	}
}