package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageGetEmergencyItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEmergencyItems extends GuiBase
{
	private long endTime = System.currentTimeMillis() + ClientQuestFile.INSTANCE.emergencyItemsCooldown.millis();
	private boolean done = false;

	private static class EmergencyItem extends Widget
	{
		private final ItemStack stack;

		public EmergencyItem(Panel p, ItemStack is)
		{
			super(p);
			stack = is;
			setSize(16, 16);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			List<String> l = stack.getTooltip(ClientUtils.MC.player, ITooltipFlag.TooltipFlags.NORMAL);
			list.add(stack.getRarity().rarityColor + l.get(0));

			for (int i = 1; i < l.size(); i++)
			{
				list.add(TextFormatting.GRAY + l.get(i));
			}
		}

		@Override
		public void draw()
		{
			GuiHelper.drawItem(stack, getAX(), getAY(), true, Icon.EMPTY);
		}
	}

	private final SimpleTextButton cancelButton = new SimpleTextButton(this, I18n.format("gui.cancel"), Icon.EMPTY)
	{
		@Override
		public void onClicked(MouseButton button)
		{
			GuiHelper.playClickSound();
			getGui().closeGui();
		}
	};

	private final Panel itemPanel = new Panel(this)
	{
		@Override
		public void addWidgets()
		{
			for (ItemStack stack : ClientQuestFile.INSTANCE.emergencyItems)
			{
				add(new EmergencyItem(this, stack));
			}
		}

		@Override
		public void alignWidgets()
		{
			setWidth(align(new WidgetLayout.Horizontal(0, 6, 0)));
			setHeight(16);
			setPos((GuiEmergencyItems.this.width - itemPanel.width) / 2, GuiEmergencyItems.this.height * 2 / 3 - 10);
		}
	};

	@Override
	public void addWidgets()
	{
		add(itemPanel);
		add(cancelButton);
		cancelButton.setPos((width - cancelButton.width) / 2, height * 2 / 3 + 16);
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	@Override
	public void drawBackground()
	{
		long left = endTime - System.currentTimeMillis();

		if (left <= 0L)
		{
			if (!done)
			{
				done = true;
				cancelButton.setTitle(I18n.format("gui.close"));
				new MessageGetEmergencyItems().sendToServer();
			}

			left = 0L;
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
		s = left <= 0L ? "00:00" : StringUtils.getTimeString(left / 1000L * 1000L + 1000L);
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