package com.feed_the_beast.ftbquests.gui;

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
import com.feed_the_beast.ftbquests.quest.EnumQuestShape;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
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
			setY(3);
			stack = is;
			setSize(16, 16);
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
			GuiHelper.addStackTooltip(stack, list);
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			EnumQuestShape.RSQUARE.outline.draw(x - 3, y - 3, w + 6, h + 6);
			GuiHelper.drawItem(stack, x, y, true);
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return stack;
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
			setWidth(align(new WidgetLayout.Horizontal(3, 7, 3)));
			setHeight(22);
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
	public void drawBackground(Theme theme, int x, int y, int w, int h)
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
		GlStateManager.translate((int) (w / 2F), (int) (h / 5F), 0F);
		GlStateManager.scale(2F, 2F, 1F);
		String s = I18n.format("ftbquests.file.emergency_items");
		theme.drawString(s, -theme.getStringWidth(s) / 2, 0, Color4I.WHITE, 0);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate((int) (w / 2F), (int) (h / 2.5F), 0F);
		GlStateManager.scale(4F, 4F, 1F);
		s = left <= 0L ? "00:00" : StringUtils.getTimeString(left / 1000L * 1000L + 1000L);
		int x1 = -theme.getStringWidth(s) / 2;
		theme.drawString(s, x1 - 1, 0, Color4I.BLACK, 0);
		theme.drawString(s, x1 + 1, 0, Color4I.BLACK, 0);
		theme.drawString(s, x1, 1, Color4I.BLACK, 0);
		theme.drawString(s, x1, -1, Color4I.BLACK, 0);
		theme.drawString(s, x1, 0, Color4I.WHITE, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}