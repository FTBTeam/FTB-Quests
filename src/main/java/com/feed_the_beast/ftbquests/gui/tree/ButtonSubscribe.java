package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonSubscribe extends Button
{
	public ButtonSubscribe(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.subscribe"), Icon.getIcon(FTBLib.MOD_ID + ":textures/icons/twitch.png"));
		setSize(12, 12);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("https://www.twitch.tv/latvianmodder/subscribe");
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (isMouseOver())
		{
			super.drawBackground(theme, x, y, w, h);
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 500);
		super.draw(theme, x, y, w, h);
		GlStateManager.popMatrix();
	}
}