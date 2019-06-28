package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class CustomTask extends Task
{
	public static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	public CustomTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.CUSTOM;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onButtonClicked(boolean canClick)
	{
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new BooleanTaskData<>(this, data);
	}
}