package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskData;
import com.feed_the_beast.ftbquests.tile.TileScreenCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ContainerTask extends Container
{
	public final QuestTaskData data;
	public TileScreenCore screen;

	public ContainerTask(EntityPlayer player, QuestTaskData d)
	{
		data = d;

		int invX = 8;
		int invY = 132;

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, invX + x * 18, invY + y * 18));
			}
		}

		for (int x = 0; x < 9; x++)
		{
			addSlotToContainer(new Slot(player.inventory, x, invX + x * 18, invY + 58));
		}
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

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player)
	{
		if (clickType == ClickType.QUICK_MOVE)
		{
			if (!data.task.canInsertItem() || data.getProgress() >= data.task.getMaxProgress())
			{
				return ItemStack.EMPTY;
			}

			Slot slot = inventorySlots.get(slotId);

			if (slot != null && slot.getHasStack())
			{
				ItemStack stack = slot.getStack();
				ItemStack prevStack = stack.copy();

				stack = data.insertItem(stack, dragType == 1, false, player);

				if (stack.isEmpty())
				{
					slot.putStack(ItemStack.EMPTY);
				}
				else
				{
					slot.putStack(stack);
				}

				return prevStack;
			}

			return ItemStack.EMPTY;
		}

		return super.slotClick(slotId, dragType, clickType, player);
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id)
	{
		if ((id == 0 || id == 1) && data.task.canInsertItem())
		{
			ItemStack stack = player.inventory.getItemStack();
			ItemStack stack1 = data.insertItem(stack, id == 1, false, player);

			if (stack != stack1)
			{
				player.inventory.setItemStack(stack1);
				return true;
			}
		}

		return false;
	}
}