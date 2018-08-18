package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.IProgressData;
import com.feed_the_beast.ftbquests.quest.QuestFile;
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

				QuestFile file = FTBQuests.PROXY.getQuestFile(chest.getWorld());

				IProgressData teamData = chest.getTeam();

				if (teamData != null)
				{
					for (int i = 0; i < file.allItemAcceptingTasks.size(); i++)
					{
						ItemStack stack1 = teamData.getQuestTaskData(file.allItemAcceptingTasks.get(i)).insertItem(stack, dragType == 1, false);

						if (stack != stack1)
						{
							stack = stack1;

							if (dragType == 1 || stack.isEmpty())
							{
								break;
							}
						}
					}
				}

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
}