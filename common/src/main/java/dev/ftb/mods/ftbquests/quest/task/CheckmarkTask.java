package dev.ftb.mods.ftbquests.quest.task;

import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;

public class CheckmarkTask extends AbstractBooleanTask {
	public CheckmarkTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CHECKMARK;
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return true;
	}

	@Override
	public boolean checkOnLogin() {
		return false;
	}

	@Override
	protected boolean hasIconConfig() {
		return false;
	}
}
