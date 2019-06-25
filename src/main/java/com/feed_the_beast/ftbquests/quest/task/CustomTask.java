package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class CustomTask extends QuestTask
{
	public CustomTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.CUSTOM;
	}

	@Override
	public void onButtonClicked(boolean canClick)
	{
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<CustomTask>
	{
		private Data(CustomTask task, QuestData data)
		{
			super(task, data);
		}

		@Override
		public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
		{
			if (progress < 1L)
			{
				if (!simulate)
				{
					progress = 1L;
					sync();
				}

				return true;
			}

			return false;
		}
	}
}