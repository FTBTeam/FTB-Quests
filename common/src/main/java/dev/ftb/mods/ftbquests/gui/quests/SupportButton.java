package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class SupportButton extends TabButton {
	public SupportButton(Panel panel) {
		super(panel, new TranslatableComponent("lat_support"), ThemeProperties.SUPPORT_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick("https://latvian.dev/supporting");
	}
}