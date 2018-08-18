package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ContainerQuestChest extends Container
{
	public final TileQuestChest chest;

	public ContainerQuestChest(EntityPlayer player, TileQuestChest c)
	{
		chest = c;

		int invX = 8;
		int invY = 84;

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
		return !chest.isInvalid();
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player)
	{
		if (clickType == ClickType.QUICK_MOVE)
		{
			Slot slot = inventorySlots.get(slotId);

			if (slot != null && slot.getHasStack())
			{
				ItemStack stack = slot.getStack();
				ItemStack prevStack = stack.copy();

				//stack = data.insertItem(stack, dragType == 1, false);

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
		if (id == 0 || id == 1)
		{
			ItemStack prevStack = player.inventory.getItemStack();
			ItemStack stack1 = prevStack;//data.insertItem(prevStack, id == 1, false);

			if (prevStack != stack1)
			{
				player.inventory.setItemStack(stack1);
				return true;
			}
		}

		return false;
	}
}