package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
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