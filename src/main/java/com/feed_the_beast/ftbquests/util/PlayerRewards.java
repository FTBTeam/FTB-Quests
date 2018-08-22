package com.feed_the_beast.ftbquests.util;

import com.feed_the_beast.ftblib.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftbquests.net.MessageSyncPlayerRewards;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class PlayerRewards implements INBTSerializable<NBTTagList>, IItemHandlerModifiable
{
	public final QuestFile file;
	public final List<ItemStack> items;
	public FTBQuestsPlayerData playerData;

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
				list.appendTag(ItemStackSerializer.write(stack));
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
			ItemStack stack = ItemStackSerializer.read(nbt.getCompoundTagAt(i));

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
			ItemStack stack = items.get(slot);

			if (amount > stack.getCount())
			{
				amount = stack.getCount();
			}

			ItemStack stack1 = ItemHandlerHelper.copyStackWithSize(stack, amount);

			if (!simulate)
			{
				stack.shrink(amount);
				setStackInSlot(slot, stack);
			}

			return stack1;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void setStackInSlot(int index, ItemStack stack)
	{
		if (index >= items.size())
		{
			if (!stack.isEmpty())
			{
				for (ItemStack stack1 : items)
				{
					if (InvUtils.stacksAreEqual(stack, stack1))
					{
						int add = Math.min(stack.getCount(), stack1.getMaxStackSize() - stack1.getCount());

						if (add > 0)
						{
							stack1.grow(add);
							stack.shrink(add);

							if (stack.isEmpty())
							{
								break;
							}
						}
					}
				}

				if (!stack.isEmpty())
				{
					items.add(stack);
				}
			}
		}
		else if (stack.isEmpty())
		{
			items.remove(index);
		}
		else
		{
			items.set(index, stack);
		}

		items.removeIf(ItemStack::isEmpty);

		if (playerData != null)
		{
			playerData.player.markDirty();

			if (playerData.player.isOnline())
			{
				new MessageSyncPlayerRewards(items).sendTo(playerData.player.getPlayer());
			}
		}
	}

	public void add(ItemStack stack)
	{
		setStackInSlot(Short.MAX_VALUE, stack);
	}
}