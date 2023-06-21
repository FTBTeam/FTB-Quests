package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.TogglePinnedMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class AutopinButton extends TabButton {
	public AutopinButton(Panel panel) {
		super(panel,
				Component.translatable(isAutoPin() ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"),
				isAutoPin() ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get()
		);
	}

	private static boolean isAutoPin() {
		return ClientQuestFile.isQuestPinned(TeamData.AUTO_PIN_ID);
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		new TogglePinnedMessage(TeamData.AUTO_PIN_ID).sendToServer();
	}
}
