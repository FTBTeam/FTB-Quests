package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.SimpleToast;

/**
 * @author LatvianModder
 */
public class CustomToast extends SimpleToast
{
	private String title;
	private Icon icon;
	private String description;

	public CustomToast(String t, Icon i, String d)
	{
		title = t;
		icon = i;
		description = d;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getSubtitle()
	{
		return description;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}
}