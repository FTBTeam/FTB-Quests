package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.client.gui.SimpleToast;
import dev.ftb.mods.ftblibrary.icon.Icon;

public class CustomToast extends SimpleToast {
	private final Component title;
	private final Icon<?> icon;
	private final Component description;

	public CustomToast(Component title, Icon<?> icon, Component description) {
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
	public Icon<?> getIcon() {
		return icon;
	}
}