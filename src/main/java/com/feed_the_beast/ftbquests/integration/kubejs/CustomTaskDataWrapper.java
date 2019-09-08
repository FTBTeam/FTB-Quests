package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;

/**
 * @author LatvianModder
 */
@DocClass(displayName = "Custom Task Data")
public class CustomTaskDataWrapper
{
	private final CustomTask.Data taskData;

	CustomTaskDataWrapper(CustomTask.Data d)
	{
		taskData = d;
	}

	@DocMethod
	public CustomTask getTask()
	{
		return taskData.task;
	}

	@DocMethod
	public QuestData getData()
	{
		return taskData.data;
	}

	@DocMethod
	public long getProgress()
	{
		return taskData.progress;
	}

	@DocMethod
	public void setProgress(long progress)
	{
		taskData.setProgress(progress);
	}
}