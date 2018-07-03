package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.tasks.ItemTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerItemTask extends ContainerTaskBase
{
	public ContainerItemTask(EntityPlayer player, ItemTask.Data d)
	{
		super(player, d);
	}

	@Override
	public void addTaskSlots()
	{
		addSlotToContainer(new SlotItemHandler((ItemTask.Data) data, 0, 80, 34)
		{
			@Override
			public void putStack(ItemStack stack)
			{
				getItemHandler().insertItem(0, stack, false);
				onSlotChanged();
			}

			@Override
			public int getSlotStackLimit()
			{
				return Math.min(super.getSlotStackLimit(), data.task.getMaxProgress() - data.getProgress());
			}
		});
	}

	@Override
	public int getNonPlayerSlots()
	{
		return 1;
	}
}