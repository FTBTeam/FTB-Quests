package com.feed_the_beast.ftbquests.quest.theme.property;

import com.feed_the_beast.ftblib.lib.icon.Color4I;

/**
 * @author LatvianModder
 */
public class ColorProperty extends ThemeProperty<Color4I>
{
	public ColorProperty(String n)
	{
		super(n);
	}

	@Override
	public Color4I parse(String string)
	{
		return Color4I.fromString(string);
	}
}