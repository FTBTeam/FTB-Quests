package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
@DocClass("Event that gets fired when a task is started")
public class TaskStartedEventJS extends EventJS
{
	public final transient TaskStartedEvent event;

	public TaskStartedEventJS(TaskStartedEvent e)
	{
		event = e;
	}

	@DocMethod
	public TaskData getTaskData()
	{
		return event.getTaskData();
	}
}