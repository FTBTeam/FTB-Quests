package com.feed_the_beast.ftbquests.gui.chest;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class ContainerQuestChest extends Container
{
	public final EntityPlayer player;
	public final QuestFile questFile;
	public final TileQuestChest chest;

	public ContainerQuestChest(EntityPlayer ep, TileQuestChest c)
	{
		player = ep;
		questFile = FTBQuests.PROXY.getQuestFile(player.world);
		chest = c;

		int invX = 8;
		int invY = 107;

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
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			slot.putStack(chest.insert(slot.getStack(), false, player));
			detectAndSendChanges();
			return ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id)
	{
		if (id == 0)
		{
			player.inventory.setItemStack(chest.insert(player.inventory.getItemStack(), false, player));
			detectAndSendChanges();
			return true;
		}

		return false;
	}
}