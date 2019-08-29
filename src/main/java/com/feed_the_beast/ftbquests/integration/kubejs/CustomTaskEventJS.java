package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocField;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
@DocClass("Custom task check override event. You can use this to have custom condition combinations for quests")
public class CustomTaskEventJS extends EventJS
{
	@DocField
	public final CustomTask task;

	@DocField("Check callback - function (player), is called every checkTimer ticks")
	public CustomTaskCheckerJS check;

	@DocField("How often in ticks the callback function should be checked")
	public int checkTimer = 1;

	@DocField("Enable checking on button click")
	public boolean enableButton = false;

	@DocField("Max progress of this task")
	public long maxProgress = 1L;

	CustomTaskEventJS(CustomTask t)
	{
		task = t;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}
}