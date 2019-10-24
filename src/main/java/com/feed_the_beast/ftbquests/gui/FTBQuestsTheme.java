package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;

/**
 * @author LatvianModder
 */
public class FTBQuestsTheme extends Theme
{
	public static final FTBQuestsTheme INSTANCE = new FTBQuestsTheme();

	@Override
	public Color4I getContentColor(WidgetType type)
	{
		if (type == WidgetType.DISABLED)
		{
			return ThemeProperties.DISABLED_TEXT_COLOR.get();
		}
		else if (type == WidgetType.MOUSE_OVER)
		{
			return ThemeProperties.HOVER_TEXT_COLOR.get();
		}

		return ThemeProperties.TEXT_COLOR.get();
	}

	@Override
	public void drawGui(int x, int y, int w, int h, WidgetType type)
	{
		ThemeProperties.BACKGROUND.get().draw(x, y, w, h);
	}

	@Override
	public void drawButton(int x, int y, int w, int h, WidgetType type)
	{
		if (type == WidgetType.DISABLED)
		{
			ThemeProperties.DISABLED_BUTTON.get().draw(x, y, w, h);
		}
		else if (type == WidgetType.MOUSE_OVER)
		{
			ThemeProperties.HOVER_BUTTON.get().draw(x, y, w, h);
		}
		else
		{
			ThemeProperties.BUTTON.get().draw(x, y, w, h);
		}
	}

	@Override
	public void drawContainerSlot(int x, int y, int w, int h)
	{
		ThemeProperties.CONTAINER_SLOT.get().draw(x, y, w, h);
	}

	@Override
	public void drawPanelBackground(int x, int y, int w, int h)
	{
		ThemeProperties.PANEL.get().draw(x, y, w, h);
	}

	@Override
	public void drawContextMenuBackground(int x, int y, int w, int h)
	{
		ThemeProperties.CONTEXT_MENU.get().draw(x, y, w, h);
	}

	@Override
	public void drawScrollBarBackground(int x, int y, int w, int h, WidgetType type)
	{
		ThemeProperties.SCROLL_BAR_BACKGROUND.get().draw(x, y, w, h);
	}

	@Override
	public void drawScrollBar(int x, int y, int w, int h, WidgetType type, boolean vertical)
	{
		ThemeProperties.SCROLL_BAR.get().draw(x, y, w, h);
	}
}