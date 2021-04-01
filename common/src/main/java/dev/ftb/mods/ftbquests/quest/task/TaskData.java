package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbquests.quest.TeamData;

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

	public String toString() {
		return teamData + "#" + task + "=" + progress;
	}
}