package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class BooleanTaskData<T extends Task> extends TaskData<T>
{
	public BooleanTaskData(T q, PlayerData d)
	{
		super(q, d);
	}

	public boolean canSubmit(ServerPlayerEntity player)
	{
		return true;
	}

	@Override
	public void submitTask(ServerPlayerEntity player, ItemStack item)
	{
		if (!isComplete() && canSubmit(player))
		{
			setProgress(1L);
		}
	}
}