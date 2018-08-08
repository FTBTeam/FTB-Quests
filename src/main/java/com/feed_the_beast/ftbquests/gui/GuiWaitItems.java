package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageGetEmergencyItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class GuiWaitItems extends GuiBase
{
	private long endTime = System.currentTimeMillis() + ClientQuestFile.INSTANCE.emergencyItemsCooldown.getTimer().millis();

	private final SimpleTextButton cancelButton = new SimpleTextButton(this, I18n.format("gui.cancel"), Icon.EMPTY)
	{
		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			getGui().closeGui();
		}
	};

	@Override
	public void addWidgets()
	{
		add(cancelButton);
	}

	@Override
	public boolean onInit()
	{
		setFullscreen();
		cancelButton.setPos((width - cancelButton.width) / 2, height * 2 / 3);
		return true;
	}

	@Override
	public void drawBackground()
	{
		long left = endTime - System.currentTimeMillis();

		if (left <= 0L)
		{
			closeGui(false);
			new MessageGetEmergencyItems().sendToServer();
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(width / 2F, height / 5F, 0F);
		GlStateManager.scale(2F, 2F, 1F);
		String s = I18n.format("ftbquests.file.emergency_items");
		drawString(s, -getStringWidth(s) / 2, 0, Color4I.WHITE, 0);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(width / 2F, height / 2.5F, 0F);
		GlStateManager.scale(4F, 4F, 1F);
		s = StringUtils.getTimeString(left / 1000L * 1000L + 1000L);
		int x = -getStringWidth(s) / 2;
		drawString(s, x - 1, 0, Color4I.BLACK, 0);
		drawString(s, x + 1, 0, Color4I.BLACK, 0);
		drawString(s, x, 1, Color4I.BLACK, 0);
		drawString(s, x, -1, Color4I.BLACK, 0);
		drawString(s, x, 0, Color4I.WHITE, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}