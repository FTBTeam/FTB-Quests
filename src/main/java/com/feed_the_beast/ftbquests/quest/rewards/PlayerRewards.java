package com.feed_the_beast.ftbquests.quest.rewards;

import com.feed_the_beast.ftbquests.quest.QuestFile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PlayerRewards implements INBTSerializable<NBTTagList>, IItemHandler
{
	public final QuestFile file;
	public final List<ItemStack> items;

	public PlayerRewards(QuestFile f)
	{
		file = f;
		items = new ArrayList<>();
	}

	@Override
	public NBTTagList serializeNBT()
	{
		NBTTagList list = new NBTTagList();

		for (ItemStack stack : items)
		{
			if (!stack.isEmpty())
			{
				list.appendTag(stack.serializeNBT());
			}
		}

		return list;
	}

	@Override
	public void deserializeNBT(NBTTagList nbt)
	{
		items.clear();

		for (int i = 0; i < nbt.tagCount(); i++)
		{
			ItemStack stack = new ItemStack(nbt.getCompoundTagAt(i));

			if (!stack.isEmpty())
			{
				items.add(stack);
			}
		}
	}

	@Override
	public int getSlots()
	{
		return 6;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return slot < 0 || slot >= items.size() ? ItemStack.EMPTY : items.get(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount > 0 && slot >= 0 && slot < items.size())
		{
			ItemStack stack0 = items.get(slot);

			if (amount > stack0.getCount())
			{
				amount = stack0.getCount();
			}

			ItemStack stack = ItemHandlerHelper.copyStackWithSize(stack0, amount);

			if (!simulate)
			{
				stack0.shrink(amount);

				if (stack0.isEmpty())
				{
					items.remove(slot);
				}
			}

			return stack;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}
}