package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.Collection;

/**
 * @author LatvianModder
 */
public class BooleanTaskData<T extends Task> extends TaskData<T>
{
	public BooleanTaskData(T q, QuestData d)
	{
		super(q, d);
	}

	public boolean canSubmit(EntityPlayerMP player)
	{
		return true;
	}

	@Override
	public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
	{
		if (!isComplete() && canSubmit(player))
		{
			if (!simulate)
			{
				setProgress(1L);
			}

			return true;
		}

		return false;
	}
}