package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;

public class CheckmarkTask extends AbstractBooleanTask {
	public CheckmarkTask(long id, Quest quest) {
		super(id, quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CHECKMARK;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawGUI(TeamData teamData, GuiGraphics graphics, int x, int y, int w, int h) {
		var icon = ThemeProperties.CHECKMARK_TASK_INACTIVE.get(this);
		if (teamData.isCompleted(this)) {
			icon = ThemeProperties.CHECKMARK_TASK_ACTIVE.get(this);
		}

		IconHelper.renderIcon(icon, graphics, x, y, w, h);
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
