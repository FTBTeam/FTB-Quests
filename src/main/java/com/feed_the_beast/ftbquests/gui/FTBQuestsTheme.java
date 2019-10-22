package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.WidgetType;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
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
		Icon icon = (type == WidgetType.DISABLED ? ThemeProperties.DISABLED_BACKGROUND : ThemeProperties.BACKGROUND).get();
		icon.draw(x, y, w, h);
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
	public void drawContextMenuBackground(int x, int y, int w, int h)
	{
		Color4I borderColor = ThemeProperties.WIDGET_BORDER.get();
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, true);
		drawGui(x + 1, y + 1, w - 2, h - 2, WidgetType.DISABLED);
		//ThemeProperties.DISABLED_BACKGROUND.get().withPadding(1).withBorder(ThemeProperties.WIDGET_BORDER.get(), true).draw(x, y, w, h);
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