package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.config.ConfigItemStack;
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
	private long endTime = System.currentTimeMillis() + ClientQuestFile.INSTANCE.emergencyItemsCooldown.getTimer().millis();

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
			for (ConfigItemStack value : ClientQuestFile.INSTANCE.emergencyItems)
			{
				add(new EmergencyItem(this, value.getStack()));
			}
		}

		@Override
		public void alignWidgets()
		{
			setWidth(align(new WidgetLayout.Horizontal(0, 6, 0)));
			setHeight(16);
			setPos((getGui().width - width) / 2, height * 2 / 3 - 10);
		}
	};

	@Override
	public void addWidgets()
	{
		add(itemPanel);
		add(cancelButton);
	}

	@Override
	public boolean onInit()
	{
		setFullscreen();
		cancelButton.setPos((width - cancelButton.width) / 2, height * 2 / 3 + 16);
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

		for (int i = 0; i < ClientQuestFile.INSTANCE.emergencyItems.list.size(); i++)
		{
			GuiHelper.drawItem(ClientQuestFile.INSTANCE.emergencyItems.list.get(i).getStack(), width / 2D - ClientQuestFile.INSTANCE.emergencyItems.list.size() * 8D - 6D + i * 24D, height * 2D / 3D - 10D, true, Icon.EMPTY);
		}
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}
}