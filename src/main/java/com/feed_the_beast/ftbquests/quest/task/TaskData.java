package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task> implements ICapabilityProvider, IItemHandler
{
	@Nullable
	public static NBTBase longToNBT(long value)
	{
		if (value <= 0L)
		{
			return null;
		}
		else if (value <= Byte.MAX_VALUE)
		{
			return new NBTTagByte((byte) value);
		}
		else if (value <= Short.MAX_VALUE)
		{
			return new NBTTagShort((short) value);
		}
		else if (value <= Integer.MAX_VALUE)
		{
			return new NBTTagInt((int) value);
		}

		return new NBTTagLong(value);
	}

	public final T task;
	public final QuestData data;
	public boolean isComplete = false;

	public long progress = 0L;

	public TaskData(T q, QuestData d)
	{
		task = q;
		data = d;
	}

	@Nullable
	public NBTBase toNBT()
	{
		return progress <= 0L ? null : longToNBT(progress);
	}

	public void fromNBT(@Nullable NBTBase nbt)
	{
		progress = nbt instanceof NBTPrimitive ? ((NBTPrimitive) nbt).getLong() : 0L;
		isComplete = isComplete();
	}

	public void changeProgress(ChangeProgress type)
	{
		if (type.reset)
		{
			progress = 0L;
			isComplete = false;
		}
		else if (type.complete)
		{
			progress = task.getMaxProgress();
		}

		sync();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return false;
	}

	@Nullable
	@Override
	public <C> C getCapability(Capability<C> capability, @Nullable EnumFacing facing)
	{
		return null;
	}

	public final int getRelativeProgress()
	{
		long max = task.getMaxProgress();

		if (max <= 0L)
		{
			return 0;
		}

		if (progress <= 0L)
		{
			return 0;
		}
		else if (progress >= max)
		{
			return 100;
		}

		return (int) Math.max(1L, (progress * 100D / (double) max));
	}

	public final boolean isComplete()
	{
		long max = task.getMaxProgress();
		return max > 0L && progress >= max;
	}

	public final boolean isStarted()
	{
		return progress > 0L && task.getMaxProgress() > 0L;
	}

	public String getProgressString()
	{
		return StringUtils.formatDouble(progress, true);
	}

	public String toString()
	{
		return data.toString() + task;
	}

	public ItemStack insertItem(ItemStack stack, boolean singleItem, boolean simulate, @Nullable EntityPlayer player)
	{
		return stack;
	}

	@Override
	public final int getSlots()
	{
		return 1;
	}

	@Override
	public final ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public final ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (task.canInsertItem() && task.getMaxProgress() > 0L && progress < task.getMaxProgress() && !stack.isEmpty())
		{
			return insertItem(stack, false, simulate, null);
		}

		return stack;
	}

	@Override
	public final ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	public final void sync()
	{
		data.syncTask(this);
	}

	public boolean submitTask(EntityPlayerMP player, Collection<ItemStack> itemsToCheck, boolean simulate)
	{
		if (!task.canInsertItem())
		{
			return false;
		}

		boolean changed = false;

		for (int i = 0; i < player.inventory.mainInventory.size(); i++)
		{
			ItemStack stack = player.inventory.mainInventory.get(i);
			ItemStack stack1 = insertItem(stack, false, simulate, player);

			if (!ItemStack.areItemStacksEqual(stack, stack1))
			{
				changed = true;

				if (!simulate)
				{
					player.inventory.mainInventory.set(i, stack1);
				}
			}
		}

		return changed;
	}

	public final boolean submitTask(EntityPlayerMP player)
	{
		return submitTask(player, Collections.emptyList(), false);
	}
}