package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

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
	public void submitTask(EntityPlayerMP player, ItemStack item)
	{
		if (!isComplete() && canSubmit(player))
		{
			setProgress(1L);
		}
	}
}