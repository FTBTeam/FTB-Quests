package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.Theme;

/**
 * @author LatvianModder
 */
public class GuiRewards extends GuiBase
{
	@Override
	public void addWidgets()
	{
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		String text = "Rewards GUI is still WIP! Use Quest Chest to get collect rewards.";
		theme.drawString(text, x + (w - theme.getStringWidth(text)) / 2, y + 8);
	}
}