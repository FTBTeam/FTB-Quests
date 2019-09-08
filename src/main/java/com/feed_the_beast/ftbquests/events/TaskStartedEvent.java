package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.task.TaskData;

/**
 * @author LatvianModder
 */
public class TaskStartedEvent extends FTBQuestsEvent
{
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