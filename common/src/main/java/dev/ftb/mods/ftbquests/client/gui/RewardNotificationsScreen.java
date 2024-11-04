package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardNotificationsScreen extends BaseScreen implements IRewardListenerScreen {
	private final Object2IntOpenHashMap<RewardKey> rewards;
	private final SimpleTextButton closeButton;
	private final Panel itemPanel;

	public RewardNotificationsScreen() {
		rewards = new Object2IntOpenHashMap<>();
		closeButton = new SimpleTextButton(this, Component.translatable("gui.close"), Color4I.empty()) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				getGui().closeGui();
			}
		};

		itemPanel = new Panel(this) {
			@Override
			public void addWidgets() {
				List<RewardKey> keys = new ArrayList<>(rewards.keySet());
				keys.sort((o1, o2) -> Integer.compare(rewards.getInt(o2), rewards.getInt(o1)));

				for (RewardKey key : keys) {
					add(new RewardNotification(this, key));
				}
			}

			@Override
			public void alignWidgets() {
				if (widgets.size() < 9) {
					setWidth(align(new WidgetLayout.Horizontal(0, 1, 0)));
					setHeight(22);
				} else {
					setWidth(23 * 9);
					setHeight(23 * Mth.ceil(widgets.size() / 9F));

					for (int i = 0; i < widgets.size(); i++) {
						widgets.get(i).setPos((i % 9) * 23, (i / 9) * 23);
					}
				}

				setPos((RewardNotificationsScreen.this.width - itemPanel.width) / 2, (RewardNotificationsScreen.this.height - itemPanel.height) / 2);
			}
		};

		setRenderBlur(false);
		itemPanel.setOnlyRenderWidgetsInside(false);
		//itemPanel.setUnicode(true);
	}

	@Override
	public void addWidgets() {
		add(itemPanel);
		add(closeButton);
		closeButton.setPos((width - closeButton.width) / 2, height * 2 / 3 + 16);
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		graphics.pose().pushPose();
		graphics.pose().translate((int) (w / 2D), (int) (h / 5D), 0);
		graphics.pose().scale(2, 2, 1);
		MutableComponent s = Component.translatable("ftbquests.rewards");
		theme.drawString(graphics, s, -theme.getStringWidth(s) / 2, 0, Color4I.WHITE, 0);
		graphics.pose().popPose();
	}

	@Override
	public Theme getTheme() {
		return FTBQuestsTheme.INSTANCE;
	}

	@Override
	public void rewardReceived(RewardKey key, int count) {
		rewards.put(key, rewards.getInt(key) + count);
		itemPanel.refreshWidgets();
	}

	private class RewardNotification extends Widget {
		private final RewardKey key;

		public RewardNotification(Panel panel, RewardKey key) {
			super(panel);

			setSize(22, 22);
			this.key = key;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (!key.getTitle().isEmpty()) {
				list.string(key.getTitle());
			}
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			QuestShape.get("rsquare").getOutline().draw(graphics, x, y, w, h);
			key.getIcon().draw(graphics, x + 3, y + 3, 16, 16);

			int count = rewards.getInt(key);

			if (count > 1) {
				graphics.pose().pushPose();
				graphics.pose().translate(0, 0, 600);
				Component s = Component.literal(StringUtils.formatDouble(count, true)).withStyle(ChatFormatting.YELLOW);
				theme.drawString(graphics, s, x + 22 - theme.getStringWidth(s), y + 12, Theme.SHADOW);
				graphics.pose().popPose();
			}
		}

		@Override
		public Optional<PositionedIngredient> getIngredientUnderMouse() {
			return PositionedIngredient.of(key.getIcon().getIngredient(), this, true);
		}
	}
}
