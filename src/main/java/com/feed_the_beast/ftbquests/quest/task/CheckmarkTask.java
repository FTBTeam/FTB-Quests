package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public class CheckmarkTask extends QuestTask
{
	public CheckmarkTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public QuestTaskType getType()
	{
		return FTBQuestsTasks.CHECKMARK;
	}

	@Override
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		(data == null || data.getProgress() < 1L ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw(x, y, w, h);
	}

	@Override
	public void drawScreen(@Nullable QuestTaskData data)
	{
		(data == null || data.getProgress() < 1L ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw3D(Icon.EMPTY);
	}

	@Override
	public QuestTaskData createData(QuestData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<CheckmarkTask>
	{
		private Data(CheckmarkTask task, QuestData data)
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