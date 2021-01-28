package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import com.feed_the_beast.ftbquests.net.MessageUpdateTaskProgress;
import com.feed_the_beast.ftbquests.quest.ChangeProgress;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.util.FTBQuestsInventoryListener;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task>
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

			if (data.file.getSide() == Env.SERVER)
			{
				if (ChangeProgress.sendUpdates)
				{
					new MessageUpdateTaskProgress(data, task.id, progress).sendToAll();
				}

				if (p == 0)
				{
					TaskStartedEvent.EVENT.invoker().accept(new TaskStartedEvent(this));
				}

				if (!taskCompleted && isComplete())
				{
					taskCompleted = true;
					List<ServerPlayer> onlineMembers = data.getOnlineMembers();
					List<ServerPlayer> notifiedPlayers;

					if (!task.quest.chapter.alwaysInvisible && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates))
					{
						notifiedPlayers = onlineMembers;
					}
					else
					{
						notifiedPlayers = Collections.emptyList();
					}

					task.onCompleted(data, onlineMembers, notifiedPlayers);

					for (ServerPlayer player : onlineMembers)
					{
						FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, task.id);
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

	public void submitTask(ServerPlayer player, ItemStack item)
	{
	}

	public final void submitTask(ServerPlayer player)
	{
		submitTask(player, ItemStack.EMPTY);
	}
}