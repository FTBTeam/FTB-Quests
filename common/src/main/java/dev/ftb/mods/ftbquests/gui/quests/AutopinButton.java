package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbquests.net.MessageTogglePinned;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class AutopinButton extends TabButton {
	public AutopinButton(Panel panel) {
		super(panel, new TranslatableComponent(((QuestScreen) panel.getGui()).file.self.getAutoPin() ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"), ((QuestScreen) panel.getGui()).file.self.getAutoPin() ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new MessageTogglePinned(1).sendToServer();
	}
}