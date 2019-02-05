package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.Quest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	public long getMaxProgress()
	{
		return 1;
	}

	@Override
	public boolean hideProgressNumbers()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawGUI(@Nullable QuestTaskData data, int x, int y, int w, int h)
	{
		(data == null || data.getProgress() < 1L ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw(x, y, w, h);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawScreen(@Nullable QuestTaskData data)
	{
		(data == null || data.getProgress() < 1L ? GuiIcons.ACCEPT_GRAY : GuiIcons.ACCEPT).draw3D(Icon.EMPTY);
	}

	@Override
	public QuestTaskData createData(ITeamData data)
	{
		return new Data(this, data);
	}

	public static class Data extends SimpleQuestTaskData<CheckmarkTask>
	{
		private Data(CheckmarkTask task, ITeamData data)
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