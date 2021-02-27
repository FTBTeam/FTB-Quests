package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.*;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class RewardNotificationsScreen extends GuiBase implements IRewardListenerScreen {
	private class RewardNotification extends Widget {
		private final RewardKey key;

		public RewardNotification(Panel p, RewardKey e) {
			super(p);
			setSize(22, 22);
			key = e;
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (!key.title.isEmpty()) {
				list.string(key.title);
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();
			QuestShape.get("rsquare").outline.draw(matrixStack, x, y, w, h);
			key.icon.draw(matrixStack, x + 3, y + 3, 16, 16);

			int count = rewards.getInt(key);

			if (count > 1) {
				matrixStack.pushPose();
				matrixStack.translate(0, 0, 600);
				MutableComponent s = new TextComponent(StringUtils.formatDouble(count, true)).withStyle(ChatFormatting.YELLOW);
				theme.drawString(matrixStack, s, x + 22 - theme.getStringWidth(s), y + 12, Theme.SHADOW);
				matrixStack.popPose();
			}
		}

		@Override
		@Nullable
		public Object getIngredientUnderMouse() {
			return new WrappedIngredient(key.icon.getIngredient()).tooltip();
		}
	}

	public final Object2IntOpenHashMap<RewardKey> rewards;
	private final SimpleTextButton closeButton;
	private final Panel itemPanel;

	public RewardNotificationsScreen() {
		rewards = new Object2IntOpenHashMap<>();
		closeButton = new SimpleTextButton(this, new TranslatableComponent("gui.close"), Icon.EMPTY) {
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
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		matrixStack.pushPose();
		matrixStack.translate((int) (w / 2F), (int) (h / 5F), 0F);
		matrixStack.scale(2, 2, 1);
		MutableComponent s = new TranslatableComponent("ftbquests.rewards");
		theme.drawString(matrixStack, s, -theme.getStringWidth(s) / 2F, 0, Color4I.WHITE, 0);
		matrixStack.popPose();
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
}