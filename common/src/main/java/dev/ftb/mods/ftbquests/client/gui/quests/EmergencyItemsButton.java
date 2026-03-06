package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftbquests.client.gui.EmergencyItemsScreen;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

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
