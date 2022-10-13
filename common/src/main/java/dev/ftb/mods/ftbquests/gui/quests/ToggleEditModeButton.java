package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author desht
 */
public class ToggleEditModeButton extends TabButton {
	public ToggleEditModeButton(Panel panel) {
		super(panel, makeTooltip(panel), ((QuestScreen) panel.getGui()).file.self.getCanEdit() ? ThemeProperties.EDITOR_ICON_ON.get() : ThemeProperties.EDITOR_ICON_OFF.get());
	}

	private static Component makeTooltip(Panel panel) {
		TeamData self = ((QuestScreen) panel.getGui()).file.self;
		String key = self.getCanEdit() ? "commands.ftbquests.editing_mode.enabled" : "commands.ftbquests.editing_mode.disabled";
		return new TranslatableComponent(key, self.name);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new ToggleEditingModeMessage().sendToServer();
	}
}