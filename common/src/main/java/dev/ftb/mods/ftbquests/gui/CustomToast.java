package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class CustomToast extends SimpleToast {
	private final Component title;
	private final Icon icon;
	private final Component description;

	public CustomToast(Component t, Icon i, Component d) {
		title = t;
		icon = i;
		description = d;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public Component getSubtitle() {
		return description;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
}