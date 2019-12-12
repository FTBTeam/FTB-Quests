package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.gui.FTBQuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageSubmitTask;
import com.feed_the_beast.ftbquests.quest.task.ItemTask;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.misc.CompactGridLayout;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiValidItems extends GuiBase
{
	public class ButtonValidItem extends Button
	{
		public final ItemStack stack;

		public ButtonValidItem(Panel panel, ItemStack is)
		{
			super(panel, "", ItemIcon.getItemIcon(is));
			stack = is;
		}

		@Override
		public void onClicked(MouseButton button)
		{
			if (ModList.get().isLoaded("jei"))
			{
				showJEIRecipe();
			}
		}

		private void showJEIRecipe()
		{
			//FIXME: FTBLibJEIIntegration.showRecipe(stack);
		}

		@Nullable
		@Override
		public Object getIngredientUnderMouse()
		{
			return new WrappedIngredient(stack).tooltip();
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			if (isMouseOver())
			{
				Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
			}

			GuiHelper.drawItem(stack, x + 2D, y + 2D, 2D, 2D, true);
		}
	}

	public final ItemTask task;
	public final List<ItemStack> validItems;
	public String title = "";
	public final boolean canClick;
	public final Panel itemPanel;
	public final Button backButton, submitButton;

	public GuiValidItems(ItemTask t, List<ItemStack> v, boolean c)
	{
		task = t;
		validItems = v;
		canClick = c;

		itemPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				for (ItemStack validItem : validItems)
				{
					add(new ButtonValidItem(this, validItem));
				}
			}

			@Override
			public void alignWidgets()
			{
				align(new CompactGridLayout(36));
				setHeight(Math.min(160, getContentHeight()));
				parent.setHeight(height + 53);
				int off = (width - getContentWidth()) / 2;

				for (Widget widget : widgets)
				{
					widget.setX(widget.posX + off);
				}

				itemPanel.setX((parent.width - width) / 2);
				backButton.setPosAndSize(itemPanel.posX - 1, height + 28, 70, 20);
				submitButton.setPosAndSize(itemPanel.posX + 75, height + 28, 70, 20);
			}

			@Override
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
				theme.drawButton(x - 1, y - 1, w + 2, h + 2, WidgetType.NORMAL);
			}
		};

		itemPanel.setPosAndSize(0, 22, 144, 0);

		backButton = new SimpleTextButton(this, I18n.format("gui.back"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				onBack();
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		submitButton = new SimpleTextButton(this, "Submit", Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				new MessageSubmitTask(task.id).sendToServer();
				onBack();
			}

			@Override
			public void addMouseOverText(List<String> list)
			{
				if (canClick && !task.consumesResources())
				{
					list.add(I18n.format("ftbquests.task.auto_detected"));
				}
			}

			@Override
			public WidgetType getWidgetType()
			{
				return canClick && task.consumesResources() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};
	}

	@Override
	public void addWidgets()
	{
		title = I18n.format("ftbquests.task.ftbquests.item.valid_for", task.getTitle());
		setWidth(Math.max(156, getTheme().getStringWidth(title) + 12));
		add(itemPanel);
		add(backButton);
		add(submitButton);
	}

	@Override
	public Theme getTheme()
	{
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		super.drawBackground(theme, x, y, w, h);
		theme.drawString(title, x + w / 2F, y + 6, Color4I.WHITE, Theme.CENTERED);
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			onBack();
		}

		return false;
	}
}