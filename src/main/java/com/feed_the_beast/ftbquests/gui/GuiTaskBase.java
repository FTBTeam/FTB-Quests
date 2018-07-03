package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiContainerWrapper;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import net.minecraft.client.gui.GuiScreen;

/**
 * @author LatvianModder
 */
public class GuiTaskBase extends GuiBase
{
	public final ContainerTaskBase container;

	public GuiTaskBase(ContainerTaskBase c)
	{
		container = c;
	}

	@Override
	public void addWidgets()
	{
	}

	@Override
	public GuiScreen getWrapper()
	{
		return new GuiContainerWrapper(this, container);
	}

	@Override
	public void drawBackground()
	{
		super.drawBackground();

		int ax = getAX();
		int ay = getAY();

		String s = container.data.task.getDisplayName();
		int sw = getStringWidth(s);

		if (!container.data.task.getIcon().isEmpty())
		{
			sw += 11;
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 14);
			container.data.task.getIcon().draw(ax + (width - sw) / 2, ay + 14, 8, 8);
			drawString(s, ax + width / 2 + 6, ay + 14, Color4I.WHITE, CENTERED);
		}
		else
		{
			Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 11, sw + 8, 13);
			drawString(s, ax + width / 2, ay + 14, Color4I.WHITE, CENTERED);
		}

		int max = container.data.task.getMaxProgress();
		int progress = Math.min(max, container.data.task.getProgress(ClientQuestList.INSTANCE));

		s = max == 0 ? "0/0 [0%]" : String.format("%d/%d [%d%%]", progress, max, (int) (progress * 100D / (double) max));
		sw = getStringWidth(s);

		Color4I.DARK_GRAY.draw(ax + (width - sw - 8) / 2, ay + 60, sw + 8, 13);
		Color4I.LIGHT_BLUE.draw(ax + (width - sw - 6) / 2, ay + 61, (sw + 6) * progress / max, 11);

		drawString(s, ax + width / 2, ay + 63, Color4I.WHITE, CENTERED);
	}
}