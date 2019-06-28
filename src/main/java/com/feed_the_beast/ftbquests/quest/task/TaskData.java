package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task> implements ICapabilityProvider, IItemHandler
{
	public final T task;
	public final QuestData data;
	public long progress = 0L;
	public boolean isComplete = false;

	public TaskData(T q, QuestData d)
	{
		task = q;
		data = d;
	}

	public final void setProgress(long p)
	{
		p = Math.max(0L, Math.min(p, task.getMaxProgress()));

		if (progress != p)
		{
			progress = p;
			isComplete = false;
			task.quest.chapter.file.clearCachedProgress();

			if (!task.quest.chapter.file.isClient())
			{
				if (ChangeProgress.sendUpdates)
				{
					new MessageUpdateTaskProgress(data.getTeamUID(), task.id, progress).sendToAll();
				}

				if (!isComplete && isComplete())
				{
					isComplete = true;
					List<EntityPlayerMP> notifyPlayers = new ArrayList<>();

					if (!task.quest.chapter.alwaysInvisible && !task.quest.canRepeat && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates))
					{
						for (ForgePlayer player : ((ServerQuestData) data).team.getMembers())
						{
							if (player.isOnline())
							{
								notifyPlayers.add(player.getPlayer());
							}
						}
					}

					task.onCompleted(data, notifyPlayers);
				}
			}

			data.markDirty();
		}
	}

	public final void addProgress(long p)
	{
		setProgress(progress + p);
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