package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.CompactGridLayout;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ValidItemsScreen extends BaseScreen {
	private final Component title;
	private final Panel itemPanel;
	private final Button backButton, submitButton;

	public ValidItemsScreen(ItemTask task, List<ItemStack> validItems, boolean canClick) {
		title = Component.translatable("ftbquests.task.ftbquests.item.valid_for", task.getTitle());

		itemPanel = new Panel(this) {
			@Override
			public void addWidgets() {
				for (ItemStack validItem : validItems) {
					add(new ValidItemButton(this, validItem));
				}
			}

			@Override
			public void alignWidgets() {
				align(new CompactGridLayout(36));
				setHeight(Math.min(160, getContentHeight()));
				parent.setHeight(height + 53);
				int off = (width - getContentWidth()) / 2;

				for (Widget widget : widgets) {
					widget.setX(widget.posX + off);
				}

				itemPanel.setX((parent.width - width) / 2);
				backButton.setPosAndSize(itemPanel.posX - 1, height + 28, 70, 20);
				submitButton.setPosAndSize(itemPanel.posX + 75, height + 28, 70, 20);
			}

			@Override
			public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
				theme.drawButton(graphics, x - 1, y - 1, w + 2, h + 2, WidgetType.NORMAL);
			}
		};

		itemPanel.setPosAndSize(0, 22, 144, 0);

		backButton = new SimpleTextButton(this, Component.translatable("gui.back"), Color4I.empty()) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				onBack();
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};

		submitButton = new SimpleTextButton(this, Component.literal("Submit"), Color4I.empty()) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				new SubmitTaskMessage(task.id).sendToServer();
				onBack();
			}

			@Override
			public void addMouseOverText(TooltipList list) {
				if (canClick && !task.consumesResources() && !task.isTaskScreenOnly()) {
					list.translate("ftbquests.task.auto_detected");
				}
			}

			@Override
			public WidgetType getWidgetType() {
				return canClick && task.consumesResources() && !task.isTaskScreenOnly() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};
	}

	@Override
	public void addWidgets() {
		setWidth(Math.max(156, getTheme().getStringWidth(title) + 12));
		add(itemPanel);
		add(backButton);
		add(submitButton);
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public void drawBackground(GuiGraphics matrixStack, Theme theme, int x, int y, int w, int h) {
		super.drawBackground(matrixStack, theme, x, y, w, h);
		theme.drawString(matrixStack, title, x + w / 2, y + 6, Color4I.WHITE, Theme.CENTERED);
	}

	@Override
	public boolean keyPressed(Key key) {
		if (super.keyPressed(key)) return true;
		if (key.esc()) {
			onBack();
			return true;
		}
		return false;
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			onBack();
		}

		return false;
	}

	private static class ValidItemButton extends Button {
		private final ItemStack stack;

		ValidItemButton(Panel panel, ItemStack stack) {
			super(panel, Component.empty(), ItemIcon.getItemIcon(stack));
			this.stack = stack;
		}

		@Override
		public void onClicked(MouseButton button) {
			FTBQuests.getRecipeModHelper().showRecipes(stack);
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(stack, this, true);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
			}

			graphics.pose().pushPose();
			graphics.pose().translate(x + w / 2D, y + h / 2D, 10);
			graphics.pose().scale(2F, 2F, 2F);
			GuiHelper.drawItem(graphics, stack, 0, true, null);
			graphics.pose().popPose();
		}
	}
}
