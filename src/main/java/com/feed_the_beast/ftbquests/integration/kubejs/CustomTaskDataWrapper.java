package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocField;

/**
 * @author LatvianModder
 */
@DocClass(displayName = "Custom Task Data")
public class CustomTaskDataWrapper
{
	@DocField
	public final CustomTask task;

	@DocField
	public final QuestData data;

	public long progress;

	CustomTaskDataWrapper(CustomTask.Data d)
	{
		task = d.task;
		data = d.data;
		progress = d.progress;
	}
}