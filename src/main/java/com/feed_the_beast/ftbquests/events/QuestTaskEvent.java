package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
@Cancelable
public class QuestTaskEvent extends FTBQuestsEvent
{
	private final Quest parent;
	private final int index;
	private final JsonObject json;
	private QuestTask task = null;

	public QuestTaskEvent(Quest c, int i, JsonObject j)
	{
		parent = c;
		index = i;
		json = j;
	}

	public Quest getParent()
	{
		return parent;
	}

	public int getIndex()
	{
		return index;
	}

	public JsonObject getJson()
	{
		return json;
	}

	@Nullable
	public QuestTask getTask()
	{
		return task;
	}

	public void setTask(QuestTask t)
	{
		task = t;
	}
}