package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;

public abstract class BooleanTask extends Task {
	public BooleanTask(Quest q) {
		super(q);
	}

	@Override
	public long calculateProgress(TeamData data) {
		return checkProgress(data) ? 1L : 0L;
	}

	public abstract boolean checkProgress(TeamData data);
}
