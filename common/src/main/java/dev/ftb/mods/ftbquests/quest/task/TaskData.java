package dev.ftb.mods.ftbquests.quest.task;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.net.MessageUpdateTaskProgress;
import dev.ftb.mods.ftbquests.quest.ChangeProgress;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.FTBQuestsInventoryListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TaskData<T extends Task> {
	public final T task;
	public final TeamData teamData;
	public long progress = 0L;
	private boolean taskCompleted = false;

	public TaskData(T q, TeamData d) {
		task = q;
		teamData = d;
	}

	public final void readProgress(long p) {
		long max = task.getMaxProgress();
		progress = Math.max(0L, Math.min(p, max));
		taskCompleted = progress == max;
	}

	public final void setProgress(long p) {
		p = Math.max(0L, Math.min(p, task.getMaxProgress()));
		long prevProgress = progress;

		if (progress != p) {
			progress = p;
			taskCompleted = false;
			task.quest.chapter.file.clearCachedProgress();

			if (teamData.file.isServerSide()) {
				Instant now = Instant.now();

				if (ChangeProgress.sendUpdates) {
					new MessageUpdateTaskProgress(teamData, task.id, progress).sendToAll();
				}

				if (prevProgress == 0) {
					task.onStarted(new QuestProgressEventData<>(now, teamData, task, teamData.getOnlineMembers(), Collections.emptyList()));
				}

				if (!taskCompleted && isComplete()) {
					taskCompleted = true;
					List<ServerPlayer> onlineMembers = teamData.getOnlineMembers();
					List<ServerPlayer> notifiedPlayers;

					if (!task.quest.chapter.alwaysInvisible && ChangeProgress.sendNotifications.get(ChangeProgress.sendUpdates)) {
						notifiedPlayers = onlineMembers;
					} else {
						notifiedPlayers = Collections.emptyList();
					}

					task.onCompleted(new QuestProgressEventData<>(now, teamData, task, onlineMembers, notifiedPlayers));

					for (ServerPlayer player : onlineMembers) {
						FTBQuestsInventoryListener.detect(player, ItemStack.EMPTY, task.id);
					}
				}
			}

			teamData.save();
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

	public final boolean isStarted() {
		return progress > 0L && task.getMaxProgress() > 0L;
	}

	public String getProgressString() {
		return StringUtils.formatDouble(progress, true);
	}

	public String toString() {
		return teamData.toString() + "#" + task;
	}

	public void submitTask(ServerPlayer player, ItemStack item) {
	}

	public final void submitTask(ServerPlayer player) {
		submitTask(player, ItemStack.EMPTY);
	}
}