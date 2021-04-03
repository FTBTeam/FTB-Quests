package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftbguilibrary.icon.Icon;
import dev.ftb.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class RewardToast extends SimpleToast {
	private final Component text;
	private final Icon icon;

	public RewardToast(Component t, Icon i) {
		text = t;
		icon = i;
	}

	@Override
	public Component getTitle() {
		return new TranslatableComponent("ftbquests.reward.collected");
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