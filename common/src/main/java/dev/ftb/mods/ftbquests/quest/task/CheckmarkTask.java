package dev.ftb.mods.ftbquests.quest.task;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbquests.quest.PlayerData;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class CheckmarkTask extends Task {
	public CheckmarkTask(Quest quest) {
		super(quest);
	}

	@Override
	public TaskType getType() {
		return TaskTypes.CHECKMARK;
	}

	@Override
	public void drawGUI(@Nullable TaskData data, PoseStack matrixStack, int x, int y, int w, int h) {
		(data == null || !data.isComplete() ? ThemeProperties.CHECKMARK_TASK_INACTIVE.get(this) : ThemeProperties.CHECKMARK_TASK_ACTIVE.get(this)).draw(matrixStack, x, y, w, h);
	}

	@Override
	public TaskData createData(PlayerData data) {
		return new BooleanTaskData<>(this, data);
	}
}