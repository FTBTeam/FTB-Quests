package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class OpenGuidesButton extends TabButton {
	public OpenGuidesButton(Panel panel) {
		super(panel, new TranslatableComponent("sidebar_button.ftbguides.guides"), ThemeProperties.GUIDE_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick("ftbguides:open_gui");
	}
}