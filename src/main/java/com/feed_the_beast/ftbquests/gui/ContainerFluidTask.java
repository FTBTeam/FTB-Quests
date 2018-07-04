package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.tasks.FluidTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerFluidTask extends ContainerTaskBase
{
	public ContainerFluidTask(EntityPlayer player, FluidTask.Data d)
	{
		super(player, d);
	}

	@Override
	public void addTaskSlots()
	{
		addSlotToContainer(new SlotItemHandler((FluidTask.Data) data, 0, 80 - 27, 34)
		{
			@Override
			public void putStack(ItemStack stack)
			{
				getItemHandler().insertItem(0, stack, false);
				onSlotChanged();
			}
		});

		addSlotToContainer(new SlotItemHandler((FluidTask.Data) data, 1, 80 + 27, 34)
		{
			@Override
			public void putStack(ItemStack stack)
			{
			}

			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}
		});
	}

	@Override
	public int getNonPlayerSlots()
	{
		return 1;
	}
}