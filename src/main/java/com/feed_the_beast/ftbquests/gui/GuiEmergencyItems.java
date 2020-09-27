package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.net.MessageGetEmergencyItems;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEmergencyItems extends GuiBase
{
	private final long endTime = System.currentTimeMillis() + ClientQuestFile.INSTANCE.emergencyItemsCooldown * 1000L;
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
		public void addMouseOverText(TooltipList list)
		{
			List<ITextComponent> list1 = new ArrayList<>();
			GuiHelper.addStackTooltip(stack, list1);

			for (ITextComponent t : list1)
			{
				list.add(t);
			}
		}

		@Override
		public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
		{
			QuestShape.RSQUARE.outline.draw(x - 3, y - 3, w + 6, h + 6);
			GuiHelper.drawItem(stack, x, y, true);
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse()
		{
			return stack;
		}
	}

	private final SimpleTextButton cancelButton = new SimpleTextButton(this, new TranslationTextComponent("gui.cancel"), Icon.EMPTY)
	{
		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
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
	public void drawBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		long left = endTime - System.currentTimeMillis();

		if (left <= 0L)
		{
			if (!done)
			{
				done = true;
				cancelButton.setTitle(new TranslationTextComponent("gui.close"));
				new MessageGetEmergencyItems().sendToServer();
			}

			left = 0L;
		}

		matrixStack.push();
		matrixStack.translate((int) (w / 2F), (int) (h / 5F), 0F);
		matrixStack.scale(2F, 2F, 1F);
		String s = I18n.format("ftbquests.file.emergency_items");
		theme.drawString(matrixStack, s, -theme.getStringWidth(s) / 2F, 0, Color4I.WHITE, 0);
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate((int) (w / 2F), (int) (h / 2.5F), 0F);
		matrixStack.scale(4F, 4F, 1F);
		s = left <= 0L ? "00:00" : StringUtils.getTimeString(left / 1000L * 1000L + 1000L);
		int x1 = -theme.getStringWidth(s) / 2;
		theme.drawString(matrixStack, s, x1 - 1, 0, Color4I.BLACK, 0);
		theme.drawString(matrixStack, s, x1 + 1, 0, Color4I.BLACK, 0);
		theme.drawString(matrixStack, s, x1, 1, Color4I.BLACK, 0);
		theme.drawString(matrixStack, s, x1, -1, Color4I.BLACK, 0);
		theme.drawString(matrixStack, s, x1, 0, Color4I.WHITE, 0);
		matrixStack.pop();
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}
}