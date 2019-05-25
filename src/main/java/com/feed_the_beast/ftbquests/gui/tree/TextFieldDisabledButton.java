package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.TextField;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;

/**
 * @author LatvianModder
 */
public class TextFieldDisabledButton extends TextField
{
	public TextFieldDisabledButton(Panel panel, String text)
	{
		super(panel);
		addFlags(Theme.CENTERED | Theme.CENTERED_V);
		setText(text);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			theme.drawButton(x, y, w, h, WidgetType.DISABLED);
		}
	}
}