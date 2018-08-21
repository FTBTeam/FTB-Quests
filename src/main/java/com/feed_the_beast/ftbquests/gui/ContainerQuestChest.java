package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.rewards.PlayerRewards;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerQuestChest extends Container
{
	private class SlotInput extends SlotItemHandler
	{
		public SlotInput(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return chest.insertItem(0, stack, true) != stack;
		}

		@Override
		public void putStack(ItemStack stack)
		{
			chest.insertItem(0, stack, false);
		}
	}

	private class SlotOutput extends SlotItemHandler
	{
		public SlotOutput(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return false;
		}

		@Override
		public void putStack(ItemStack stack)
		{
		}
	}

	public final TileQuestChest chest;

	public ContainerQuestChest(EntityPlayer player, TileQuestChest c)
	{
		chest = c;

		int invX = 8;
		int invY = 107;

		addSlotToContainer(new SlotInput(c, 0, 8, 84));

		PlayerRewards rewards = FTBQuests.PROXY.getQuestFile(c.getWorld()).getRewards(player);

		for (int i = 0; i < 6; i++)
		{
			addSlotToContainer(new SlotOutput(rewards, i, 44 + i * 18, 84));
		}

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
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		if (index <= 0)
		{
			return ItemStack.EMPTY;
		}
		else if (index <= 6)
		{
			Slot slot = inventorySlots.get(index);

			if (slot.getHasStack())
			{
			}

			return ItemStack.EMPTY;
		}
		else if (index < inventorySlots.size())
		{
			Slot slot = inventorySlots.get(index);

			if (slot.getHasStack())
			{
			}
		}

		return ItemStack.EMPTY;
	}
}