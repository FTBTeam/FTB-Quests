package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.net.ClaimAllRewardsMessage;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public class CollectRewardsButton extends TabButton {
	public CollectRewardsButton(Panel panel) {
		super(panel, Component.empty(), ThemeProperties.COLLECT_REWARDS_ICON.get());
		title = questScreen.file.getTitle();
	}

	@Override
	public void onClicked(MouseButton button) {
		if (FTBQuestsClient.getClientPlayerData().hasUnclaimedRewards(ClientUtils.getClientPlayer().getUUID(), questScreen.file)) {
			playClickSound();
			new RewardNotificationsScreen().openGui();
			NetworkManager.sendToServer(ClaimAllRewardsMessage.INSTANCE);
		}
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		list.translate("ftbquests.gui.collect_rewards");
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		super.draw(graphics, theme, x, y, w, h);

		if (FTBQuestsClient.getClientPlayerData().hasUnclaimedRewards(ClientUtils.getClientPlayer().getUUID(), questScreen.file)) {
			int s = w / 2;
			graphics.pose().pushMatrix();
			graphics.pose().translate(x + w - s, y);
			IconHelper.renderIcon(ThemeProperties.ALERT_ICON.get(questScreen.file), graphics, 0, 0, s, s);
			graphics.pose().popMatrix();
		}
	}
}
