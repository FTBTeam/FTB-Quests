package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.misc.SimpleToast;
import com.feed_the_beast.ftblib.lib.icon.Icon;

/**
 * @author LatvianModder
 */
public class ToastCustom extends SimpleToast
{
	private String title;
	private Icon icon;
	private String description;

	public ToastCustom(String t, Icon i, String d)
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