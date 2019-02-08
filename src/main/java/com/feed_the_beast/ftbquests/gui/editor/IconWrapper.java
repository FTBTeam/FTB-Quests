package com.feed_the_beast.ftbquests.gui.editor;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.icon.Color4I;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class IconWrapper
{
	private static final Map<com.feed_the_beast.ftblib.lib.icon.Icon, ImageIcon> CACHE = new HashMap<>();

	public static void clearCache()
	{
		CACHE.clear();
	}

	public static ImageIcon from(@Nullable com.feed_the_beast.ftblib.lib.icon.Icon icon)
	{
		if (icon == null)
		{
			return from(GuiIcons.REMOVE_GRAY);
		}

		try
		{
			if (icon.canBeCached())
			{
				ImageIcon i = CACHE.get(icon);

				if (i == null)
				{
					try
					{
						i = new ImageIcon(icon.readImage());
					}
					catch (Exception ex)
					{
						i = new ImageIcon(Color4I.BLACK.readImage());
					}

					CACHE.put(icon, i);
				}

				return i;
			}
			else
			{
				return new ImageIcon(icon.readImage());
			}
		}
		catch (Exception ex)
		{
			return from(GuiIcons.REMOVE_GRAY);
		}
	}
}