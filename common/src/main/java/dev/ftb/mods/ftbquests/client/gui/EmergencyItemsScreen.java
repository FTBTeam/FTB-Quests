package dev.ftb.mods.ftbquests.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TimeUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.GetEmergencyItemsMessage;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmergencyItemsScreen extends BaseScreen {
	private final long endTime = System.currentTimeMillis() + ClientQuestFile.INSTANCE.getEmergencyItemsCooldown() * 1000L;
	private boolean done = false;

	@Override
	public void addWidgets() {
		add(itemPanel);
		add(cancelButton);
		cancelButton.setPos((width - cancelButton.width) / 2, height * 2 / 3 + 16);
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		long timeLeft = endTime - System.currentTimeMillis();

		if (timeLeft <= 0L) {
			if (!done) {
				done = true;
				cancelButton.setTitle(Component.translatable("gui.close"));
				new GetEmergencyItemsMessage().sendToServer();
			}

			timeLeft = 0L;
		}

		PoseStack poseStack = graphics.pose();

		poseStack.pushPose();
		poseStack.translate((int) (w / 2D), (int) (h / 5D), 0);
		poseStack.scale(2F, 2F, 1F);
		String s = I18n.get("ftbquests.file.emergency_items");
		theme.drawString(graphics, s, -theme.getStringWidth(s) / 2, 0, Color4I.WHITE, 0);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate((int) (w / 2D), (int) (h / 2.5D), 0);
		poseStack.scale(4F, 4F, 1F);
		s = timeLeft <= 0L ? "00:00" : TimeUtils.getTimeString(timeLeft / 1000L * 1000L + 1000L);
		int x1 = -theme.getStringWidth(s) / 2;
		theme.drawString(graphics, s, x1 - 1, 0, Color4I.BLACK, 0);
		theme.drawString(graphics, s, x1 + 1, 0, Color4I.BLACK, 0);
		theme.drawString(graphics, s, x1, 1, Color4I.BLACK, 0);
		theme.drawString(graphics, s, x1, -1, Color4I.BLACK, 0);
		theme.drawString(graphics, s, x1, 0, Color4I.WHITE, 0);
		poseStack.popPose();
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	private static class EmergencyItemWidget extends Widget {
		private final ItemStack stack;

		public EmergencyItemWidget(Panel panel, ItemStack stack) {
			super(panel);

			this.stack = stack;

			setY(3);
			setSize(16, 16);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			List<Component> l = new ArrayList<>();
			GuiHelper.addStackTooltip(stack, l);
			l.forEach(list::add);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			QuestShape.get("rsquare").getOutline().draw(graphics, x - 3, y - 3, w + 6, h + 6);
			graphics.pose().pushPose();
			graphics.pose().translate(x + w / 2D, y + h / 2D, 100);
			GuiHelper.drawItem(graphics, stack, 0, true, null);
			graphics.pose().popPose();
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(stack, this);
		}
	}

	private final SimpleTextButton cancelButton = new SimpleTextButton(this, Component.translatable("gui.cancel"), Color4I.empty()) {
		@Override
		public void onClicked(MouseButton button) {
			playClickSound();
			getGui().closeGui();
		}
	};

	private final Panel itemPanel = new Panel(this) {
		@Override
		public void addWidgets() {
			ClientQuestFile.INSTANCE.getEmergencyItems()
					.forEach(stack -> add(new EmergencyItemWidget(this, stack)));
		}

		@Override
		public void alignWidgets() {
			setWidth(align(new WidgetLayout.Horizontal(3, 7, 3)));
			setHeight(22);
			setPos((EmergencyItemsScreen.this.width - itemPanel.width) / 2, EmergencyItemsScreen.this.height * 2 / 3 - 10);
		}
	};
}
