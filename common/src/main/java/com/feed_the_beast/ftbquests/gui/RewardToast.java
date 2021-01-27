package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class RewardToast extends SimpleToast
{
	private final Component text;
	private final Icon icon;

	public RewardToast(Component t, Icon i)
	{
		text = t;
		icon = i;
	}

	@Override
	public Component getTitle()
	{
		return new TranslatableComponent("ftbquests.reward.collected");
	}

	@Override
	public Component getSubtitle()
	{
		return text;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}
}