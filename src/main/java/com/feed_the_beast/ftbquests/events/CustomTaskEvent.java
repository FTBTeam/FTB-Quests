package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * @author LatvianModder
 */
@Cancelable
public class CustomTaskEvent extends FTBQuestsEvent
{
	private final CustomTask task;

	public CustomTaskEvent(CustomTask t)
	{
		task = t;
	}

	public CustomTask getTask()
	{
		return task;
	}
}