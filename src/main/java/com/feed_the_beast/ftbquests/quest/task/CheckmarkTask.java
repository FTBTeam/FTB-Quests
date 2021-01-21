package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.mojang.blaze3d.vertex.PoseStack;
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
	public void drawGUI(@Nullable TaskData data, PoseStack matrixStack, int x, int y, int w, int h)
	{
		(data == null || !data.isComplete() ? ThemeProperties.CHECKMARK_TASK_INACTIVE.get(this) : ThemeProperties.CHECKMARK_TASK_ACTIVE.get(this)).draw(matrixStack, x, y, w, h);
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}