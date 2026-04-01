package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class OpenShopButton extends TabButton {
	public OpenShopButton(Panel panel) {
		super(panel, Component.translatable("sidebar_button.ftbmoney.shop"), ThemeProperties.SHOP_ICON.get());
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		list.add(getTitle());
		// TODO do we want a "money" team property ?
//		list.add(Component.literal(String.format("◎ %,d", FTBTeamsAPI.api().getClientManager().self().extraData().getLong("Money"))).withStyle(ChatFormatting.GOLD));
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}
