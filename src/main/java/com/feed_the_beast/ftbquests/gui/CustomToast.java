package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;
import net.minecraft.util.text.ITextComponent;

/**
 * @author LatvianModder
 */
public class CustomToast extends SimpleToast
{
	private final ITextComponent title;
	private final Icon icon;
	private final ITextComponent description;

	public CustomToast(ITextComponent t, Icon i, ITextComponent d)
	{
		title = t;
		icon = i;
		description = d;
	}

	@Override
	public ITextComponent getTitle()
	{
		return title;
	}

	@Override
	public ITextComponent getSubtitle()
	{
		return description;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}
}