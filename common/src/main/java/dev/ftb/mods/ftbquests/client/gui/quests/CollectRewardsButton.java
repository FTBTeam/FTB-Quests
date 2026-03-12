package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.RewardSelectorScreen;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public class CollectRewardsButton extends TabButton {
	public CollectRewardsButton(Panel panel) {
		super(panel, Component.empty(), ThemeProperties.COLLECT_REWARDS_ICON.get());
		title = questScreen.file.getTitle();
	}

	@Override
	public void onClicked(MouseButton button) {
		if (anyUnclaimedRewards()) {
			playClickSound();
			new RewardSelectorScreen(questScreen.file.selfTeamData, questScreen).openGui();
		}
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		if (anyUnclaimedRewards()) {
			list.translate("ftbquests.gui.reward_selector");
		} else {
			list.translate("ftbquests.gui.no_rewards");

		}
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		Color4I c = Color4I.WHITE.withAlpha(anyUnclaimedRewards() ? 255 : 128);
		IconHelper.renderIcon(icon.withColor(c), graphics, x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver()) {
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(questScreen.selectedChapter);
			IconHelper.renderIcon(backgroundColor, graphics, x + 1, y, w - 2, h);
		}

		if (anyUnclaimedRewards()) {
			int s = w / 3 + 1;
			graphics.pose().pushMatrix();
			graphics.pose().translate(x + w - s, y);
			IconHelper.renderIcon(ThemeProperties.ALERT_ICON.get(questScreen.file), graphics, 0, 0, s, s);
			graphics.pose().popMatrix();
		}
	}

	private boolean anyUnclaimedRewards() {
		return questScreen.file.selfTeamData.hasUnclaimedRewards(ClientUtils.getClientPlayer().getUUID(), questScreen.file);
	}
}
