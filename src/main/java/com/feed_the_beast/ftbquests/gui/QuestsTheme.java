package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;

/**
 * @author LatvianModder
 */
public class QuestsTheme extends Theme
{
	public static final QuestsTheme INSTANCE = new QuestsTheme();

	@Override
	public void drawGui(int x, int y, int w, int h, WidgetType type)
	{
		Color4I.BLACK.withAlpha(150).withOutline(Color4I.WHITE.withAlpha(88), false).draw(x, y, w, h);
	}

	@Override
	public void drawContainerSlot(int x, int y, int w, int h)
	{
		Icon.EMPTY.withOutline(Color4I.WHITE.withAlpha(150), false).draw(x - 1, y - 1, w + 2, h + 2);
	}

	@Override
	public void drawContextMenuBackground(int x, int y, int w, int h)
	{
		Color4I.DARK_GRAY.withAlpha(200).withOutline(Color4I.GRAY.withAlpha(200), true).draw(x, y, w, h);
	}
}