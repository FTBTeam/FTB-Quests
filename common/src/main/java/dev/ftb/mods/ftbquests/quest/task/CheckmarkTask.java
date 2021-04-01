package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class CheckmarkTask extends BooleanTask {
	public CheckmarkTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CHECKMARK;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawGUI(TeamData teamData, PoseStack matrixStack, int x, int y, int w, int h) {
		(teamData.isCompleted(this) ? ThemeProperties.CHECKMARK_TASK_INACTIVE.get(this) : ThemeProperties.CHECKMARK_TASK_ACTIVE.get(this)).draw(matrixStack, x, y, w, h);
	}

	@Override
	public boolean canSubmit(TeamData teamData, ServerPlayer player) {
		return true;
	}
}