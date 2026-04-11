package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public class RewardToast extends SimpleToast {
	private final Component title;
	private final Component text;
	private final Icon icon;

	public RewardToast(Component text, Icon icon) {
		this(Component.translatable("ftbquests.reward.collected"), text, icon);
	}

	public RewardToast(Component title, Component text, Icon icon) {
		this.title = title;
		this.text = text;
		this.icon = icon;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public Component getSubtitle() {
		return text;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}
}
