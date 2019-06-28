package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class CheckmarkTask extends Task
{
	public CheckmarkTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.CHECKMARK;
	}

	@Override
	public void drawGUI(@Nullable TaskData data, int x, int y, int w, int h)
	{
		(data == null || !data.isComplete() ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw(x, y, w, h);
	}

	@Override
	public void drawScreen(@Nullable TaskData data)
	{
		(data == null || !data.isComplete() ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw3D(Icon.EMPTY);
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}