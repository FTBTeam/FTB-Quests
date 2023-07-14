package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class DisabledButtonTextField extends TextField {
	public DisabledButtonTextField(Panel panel, Component text) {
		super(panel);
		addFlags(Theme.CENTERED | Theme.CENTERED_V);
		setText(text);
	}
}