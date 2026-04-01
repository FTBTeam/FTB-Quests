package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;

public class OpenGuidesButton extends TabButton {
	public OpenGuidesButton(Panel panel) {
		super(panel, Component.translatable("sidebar_button.ftbguides.guides"), ThemeProperties.GUIDE_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick("ftbguides:open_gui");
	}
}
