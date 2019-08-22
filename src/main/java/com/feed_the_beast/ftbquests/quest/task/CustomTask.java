package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class CustomTask extends Task
{
	public static final Predicate<QuestObjectBase> PREDICATE = object -> object instanceof CustomTask;

	@FunctionalInterface
	public interface Check
	{
		boolean check(EntityPlayerMP player);
	}

	public Check check;

	public CustomTask(Quest quest)
	{
		super(quest);
		check = null;
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
	public boolean autoSubmitOnPlayerTick()
	{
		return check != null;
	}

	@Override
	public TaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<CustomTask>
	{
		private Data(CustomTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(EntityPlayerMP player)
		{
			return task.check == null || task.check.check(player);
		}
	}
}