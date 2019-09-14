package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.Ignore;
import dev.latvian.kubejs.documentation.Info;
import dev.latvian.kubejs.event.EventJS;

/**
 * @author LatvianModder
 */
@Info("Custom task check override event. You can use this to have custom condition combinations for quests")
public class CustomTaskEventJS extends EventJS
{
	@Ignore
	public final CustomTaskEvent event;

	CustomTaskEventJS(CustomTaskEvent e)
	{
		event = e;
	}

	@Override
	public boolean canCancel()
	{
		return true;
	}

	public CustomTask getTask()
	{
		return event.getTask();
	}

	@Info("Check callback - function (player), is called every x ticks. You can change x with setCheckTimer()")
	public void setCheck(CustomTaskCheckerJS c)
	{
		getTask().check = new CheckWrapper(c);
	}

	@Info("How often in ticks the callback function should be checked")
	public void setCheckTimer(int t)
	{
		getTask().checkTimer = t;
	}

	@Info("Enable checking on button click")
	public void setEnableButton(boolean b)
	{
		getTask().enableButton = b;
	}

	@Info("Max progress of this task")
	public void setMaxProgress(long max)
	{
		getTask().maxProgress = max;
	}
}