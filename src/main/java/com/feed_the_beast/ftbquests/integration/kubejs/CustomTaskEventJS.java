package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.DocClass;
import dev.latvian.kubejs.documentation.DocMethod;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
@DocClass("Custom task check override event. You can use this to have custom condition combinations for quests")
public class CustomTaskEventJS extends EventJS
{
	public final transient CustomTaskEvent event;

	CustomTaskEventJS(CustomTaskEvent e)
	{
		event = e;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}

	@DocMethod
	public CustomTask getTask()
	{
		return event.getTask();
	}

	@DocMethod("Check callback - function (player), is called every x ticks. You can change x with setCheckTimer()")
	public void setCheck(CustomTaskCheckerJS c)
	{
		getTask().check = new CheckWrapper(c);
	}

	@DocMethod("How often in ticks the callback function should be checked")
	public void setCheckTimer(int t)
	{
		getTask().checkTimer = t;
	}

	@DocMethod("Enable checking on button click")
	public void setEnableButton(boolean b)
	{
		getTask().enableButton = b;
	}

	@DocMethod("Max progress of this task")
	public void setMaxProgress(long max)
	{
		getTask().maxProgress = max;
	}
}