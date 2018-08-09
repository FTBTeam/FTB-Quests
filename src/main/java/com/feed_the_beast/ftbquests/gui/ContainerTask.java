package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.ContainerBase;
import com.feed_the_beast.ftbquests.block.TileScreenCore;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerTask extends ContainerBase
{
	public final QuestTaskData data;
	public TileScreenCore screen;

	public ContainerTask(EntityPlayer player, QuestTaskData d)
	{
		super(player);
		data = d;

		if (d.canInsertItem())
		{
			addSlotToContainer(new SlotItemHandler(data, 0, 80, 34)
			{
				@Override
				public void putStack(ItemStack stack)
				{
					getItemHandler().insertItem(0, stack, false);
					onSlotChanged();
				}
			});
		}

		addPlayerSlots(8, 84);
	}

	@Override
	public int getNonPlayerSlots()
	{
		return data.canInsertItem() ? 1 : 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		if (screen != null && screen.isInvalid())
		{
			return false;
		}

		return !data.task.invalid;
	}
}