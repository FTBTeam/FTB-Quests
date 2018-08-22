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
	public void drawBackground()
	{
		String text = "Rewards GUI is still WIP! Use Quest Chest to get collect rewards.";
		drawString(text, getAX() + (width - getStringWidth(text)) / 2, getAY() + 8);
	}
}