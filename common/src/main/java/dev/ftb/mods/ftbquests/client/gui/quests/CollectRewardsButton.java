package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.RewardNotificationsScreen;
import dev.ftb.mods.ftbquests.net.ClaimAllRewardsMessage;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class CollectRewardsButton extends TabButton {
	public CollectRewardsButton(Panel panel) {
		super(panel, Component.empty(), ThemeProperties.COLLECT_REWARDS_ICON.get());
		title = questScreen.file.getTitle();
	}

	@Override
	public void onClicked(MouseButton button) {
		if (questScreen.file.selfTeamData.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), questScreen.file)) {
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

		if (questScreen.file.selfTeamData.hasUnclaimedRewards(Minecraft.getInstance().player.getUUID(), questScreen.file)) {
//			GuiHelper.setupDrawing();
			int s = w / 2;//(int) (treeGui.getZoom() / 2 * quest.size);
			graphics.pose().pushMatrix();
			graphics.pose().translate(x + w - s, y);
			ThemeProperties.ALERT_ICON.get(questScreen.file).draw(graphics, 0, 0, s, s);
			graphics.pose().popMatrix();
		}
	}
}
