package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task> implements ICapabilityProvider
{
	public final T task;
	public final PlayerData data;
	public long progress = 0L;
	private boolean taskCompleted = false;

	public TaskData(T q, PlayerData d)
	{
		task = q;
		data = d;
	}

	public final void readProgress(long p)
	{
		long max = task.getMaxProgress();
		progress = Math.max(0L, Math.min(p, max));
		taskCompleted = progress == max;
	}

	public final void setProgress(long p)
	{
		p = Math.max(0L, Math.min(p, task.getMaxProgress()));

		if (progress != p)
		{
			progress = p;
			taskCompleted = false;
			task.quest.chapter.file.clearCachedProgress();

			if (data.file.getSide().isServer())
			{
				if (ChangeProgress.sendUpdates)
				{
					new MessageUpdateTaskProgress(data, task.id, progress).sendToAll();
				}

				if (p == 0)
				{
					MinecraftForge.EVENT_BUS.post(new TaskStartedEvent(this));
				}

				if (!taskCompleted && isComplete())
				{
					taskCompleted = true;
					List<ServerPlayerEntity> onlineMembers = data.getOnlineMembers();
					List<ServerPlayerEntity> notifiedPlayers;

					if (!task.quest.chapter.alwaysInvisible && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates))
					{
						notifiedPlayers = onlineMembers;
					}
					else
					{
						notifiedPlayers = Collections.emptyList();
					}

					task.onCompleted(data, onlineMembers, notifiedPlayers);

					for (ServerPlayerEntity player : onlineMembers)
					{
						FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY);
					}
				}
			}

			data.save();
		}
	}

	public final void addProgress(long p)
	{
		setProgress(progress + p);
	}

	@Override
	public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing)
	{
		return LazyOptional.empty();
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

	public void submitTask(ServerPlayerEntity player, ItemStack item)
	{
	}

	public final void submitTask(ServerPlayerEntity player)
	{
		submitTask(player, ItemStack.EMPTY);
	}
}