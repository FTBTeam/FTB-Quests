package com.feed_the_beast.ftbquests.quest.theme.property;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;

/**
 * @author LatvianModder
 */
public class IconProperty extends ThemeProperty<Icon>
{
	public final Icon builtin;

	public IconProperty(String n, Icon b)
	{
		super(n);
		builtin = b;
	}

	public IconProperty(String n)
	{
		this(n, Icon.EMPTY);
	}

	@Override
	public Icon parse(String string)
	{
		if (string.equals("builtin"))
		{
			return builtin;
		}

		return Icon.getIcon(string);
	}
}