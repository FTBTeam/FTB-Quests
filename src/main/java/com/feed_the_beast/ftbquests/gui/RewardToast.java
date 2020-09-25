package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class RewardToast extends SimpleToast
{
	private final ITextComponent text;
	private final Icon icon;

	public RewardToast(ITextComponent t, Icon i)
	{
		text = t;
		icon = i;
	}

	@Override
	public ITextComponent getTitle()
	{
		return new TranslationTextComponent("ftbquests.reward.collected");
	}

	@Override
	public ITextComponent getSubtitle()
	{
		return text;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}
}