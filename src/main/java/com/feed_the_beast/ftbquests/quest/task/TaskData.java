package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftblib.FTBLibConfig;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.ftbquests.util.ServerQuestData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
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
	private boolean taskCompleted = false;

	public TaskData(T q, QuestData d)
	{
		task = q;
		data = d;
	}

	public final void readProgress(long p)
	{
		progress = Math.max(0L, Math.min(p, task.getMaxProgress()));
		taskCompleted = isComplete();
	}

	public final void setProgress(long p)
	{
		p = Math.max(0L, Math.min(p, task.getMaxProgress()));

		if (progress != p)
		{
			long prevProgress = progress;
			progress = p;
			taskCompleted = false;
			task.quest.chapter.file.clearCachedProgress();

			if (data instanceof ServerQuestData)
			{
				if (ChangeProgress.sendUpdates)
				{
					new MessageUpdateTaskProgress(data.getTeamUID(), task.id, progress).sendToAll();
				}

				if (p == 0)
				{
					MinecraftForge.EVENT_BUS.post(new TaskStartedEvent(this));
				}

				if (FTBLibConfig.debugging.print_more_info)
				{
					FTBQuests.LOGGER.info("Changed progress for " + data.getTeamID() + ":" + task + " (" + task.getType().getRegistryName() + ") from " + prevProgress + " to " + progress + "/" + task.getMaxProgress());
				}

				if (!taskCompleted && isComplete())
				{
					taskCompleted = true;
					List<EntityPlayerMP> onlineMembers = ((ServerQuestData) data).team.getOnlineMembers();
					List<EntityPlayerMP> notifiedPlayers;

					if (!task.quest.chapter.alwaysInvisible && !task.quest.canRepeat && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates))
					{
						notifiedPlayers = onlineMembers;
					}
					else
					{
						notifiedPlayers = Collections.emptyList();
					}

					task.onCompleted(data, onlineMembers, notifiedPlayers);

					if (progress > prevProgress)
					{
						for (EntityPlayerMP player : onlineMembers)
						{
							FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, task.id);
						}
					}
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
		return data.toString() + "#" + task;
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

	public void submitTask(EntityPlayerMP player, ItemStack item)
	{
		if (!task.canInsertItem() || !item.isEmpty())
		{
			return;
		}

		boolean changed = false;

		for (int i = 0; i < player.inventory.mainInventory.size(); i++)
		{
			ItemStack stack = player.inventory.mainInventory.get(i);
			ItemStack stack1 = insertItem(stack, false, false, player);

			if (!ItemStack.areItemStacksEqual(stack, stack1))
			{
				changed = true;
				player.inventory.mainInventory.set(i, stack1);
			}
		}

		if (changed)
		{
			player.inventory.markDirty();
			player.openContainer.detectAndSendChanges();
		}
	}

	public final void submitTask(EntityPlayerMP player)
	{
		submitTask(player, ItemStack.EMPTY);
	}
}