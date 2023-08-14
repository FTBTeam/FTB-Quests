package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.gui.EmergencyItemsScreen;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;

public class EmergencyItemsButton extends TabButton {
	public EmergencyItemsButton(Panel panel) {
		super(panel, Component.translatable("ftbquests.file.emergency_items"), ThemeProperties.EMERGENCY_ITEMS_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new EmergencyItemsScreen().openGui();
	}
}
