package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.CompactGridLayout;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import dev.ftb.mods.ftbquests.gui.FTBQuestsTheme;
import dev.ftb.mods.ftbquests.integration.jei.FTBQuestsJEIHelper;
import dev.ftb.mods.ftbquests.net.SubmitTaskPacket;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ValidItemsScreen extends BaseScreen {
	public static class ValidItemButton extends Button {
		public final ItemStack stack;

		public ValidItemButton(Panel panel, ItemStack is) {
			super(panel, TextComponent.EMPTY, ItemIcon.getItemIcon(is));
			stack = is;
		}

		@Override
		public void onClicked(MouseButton button) {
			if (Platform.isModLoaded("jei")) {
				showJEIRecipe();
			}
		}

		private void showJEIRecipe() {
			FTBQuestsJEIHelper.showRecipes(stack);
		}

		@Nullable
		@Override
		public Object getIngredientUnderMouse() {
			return new WrappedIngredient(stack).tooltip();
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}

			matrixStack.pushPose();
			matrixStack.translate(0, 0, 10);
			GuiHelper.drawItem(matrixStack, stack, x + 2, y + 2, 2, 2, true, null);
			matrixStack.popPose();
		}
	}

	public final ItemTask task;
	public final List<ItemStack> validItems;
	public String title = "";
	public final boolean canClick;
	public final Panel itemPanel;
	public final Button backButton, submitButton;

	public ValidItemsScreen(ItemTask t, List<ItemStack> v, boolean c) {
		task = t;
		validItems = v;
		canClick = c;

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
			public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
				theme.drawButton(matrixStack, x - 1, y - 1, w + 2, h + 2, WidgetType.NORMAL);
			}
		};

		itemPanel.setPosAndSize(0, 22, 144, 0);

		backButton = new SimpleTextButton(this, new TranslatableComponent("gui.back"), Icon.EMPTY) {
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

		submitButton = new SimpleTextButton(this, new TextComponent("Submit"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				new SubmitTaskPacket(task.id).sendToServer();
				onBack();
			}

			@Override
			public void addMouseOverText(TooltipList list) {
				if (canClick && !task.consumesResources()) {
					list.translate("ftbquests.task.auto_detected");
				}
			}

			@Override
			public WidgetType getWidgetType() {
				return canClick && task.consumesResources() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};
	}

	@Override
	public void addWidgets() {
		title = new TranslatableComponent("ftbquests.task.ftbquests.item.valid_for", task.getTitle()).getString();
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
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.drawBackground(matrixStack, theme, x, y, w, h);
		theme.drawString(matrixStack, title, x + w / 2F, y + 6, Color4I.WHITE, Theme.CENTERED);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			onBack();
		}

		return false;
	}
}