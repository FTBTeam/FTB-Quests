package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * @author desht
 */
public class ToggleEditModeButton extends TabButton {
	public ToggleEditModeButton(Panel panel) {
		super(panel, makeTooltip(), ClientQuestFile.canClientPlayerEdit() ? ThemeProperties.EDITOR_ICON_ON.get() : ThemeProperties.EDITOR_ICON_OFF.get());
	}

	private static Component makeTooltip() {
		String key = ClientQuestFile.canClientPlayerEdit() ? "commands.ftbquests.editing_mode.enabled" : "commands.ftbquests.editing_mode.disabled";
		return Component.translatable(key, ClientQuestFile.INSTANCE.self.name);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();

		if (!questScreen.file.self.getCanEdit(Minecraft.getInstance().player)) {
			StructureTask.maybeRequestStructureSync();
		}

		new ToggleEditingModeMessage().sendToServer();
	}
}