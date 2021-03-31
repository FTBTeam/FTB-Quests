package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task> {
	public final T task;
	public final TeamData teamData;
	public long progress = 0L;

	public TaskData(T q, TeamData d) {
		task = q;
		teamData = d;
	}

	public final void setProgress(long p) {
		p = Math.max(0L, Math.min(p, task.getMaxProgress()));
		long prevProgress = progress;

		if (progress != p) {
			progress = p;
			teamData.progressChanged(this, prevProgress);
		}
	}

	public final void addProgress(long p) {
		setProgress(progress + p);
	}

	public final void complete() {
		setProgress(task.getMaxProgress());
	}

	public final int getRelativeProgress() {
		long max = task.getMaxProgress();

		if (max <= 0L) {
			return 0;
		}

		if (progress <= 0L) {
			return 0;
		} else if (progress >= max) {
			return 100;
		}

		return (int) Math.max(1L, (progress * 100D / (double) max));
	}

	public final boolean isComplete() {
		long max = task.getMaxProgress();
		return max > 0L && progress >= max;
	}

	public String getProgressString() {
		return StringUtils.formatDouble(progress, true);
	}

	public String toString() {
		return teamData + "#" + task + "=" + progress;
	}

	public void submitTask(ServerPlayer player, ItemStack item) {
	}

	public final void submitTask(ServerPlayer player) {
		submitTask(player, ItemStack.EMPTY);
	}
}