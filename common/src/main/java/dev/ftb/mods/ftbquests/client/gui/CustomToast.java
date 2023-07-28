package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import net.minecraft.network.chat.Component;

public class CustomToast extends SimpleToast {
	private final Component title;
	private final Icon icon;
	private final Component description;

	public CustomToast(Component title, Icon icon, Component description) {
		this.title = title;
		this.icon = icon;
		this.description = description;
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