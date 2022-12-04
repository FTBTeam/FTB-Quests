package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.KnownClientPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class OpenShopButton extends TabButton {
	public OpenShopButton(Panel panel) {
		super(panel, new TranslatableComponent("sidebar_button.ftbmoney.shop"), ThemeProperties.SHOP_ICON.get());
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		list.add(getTitle());
		KnownClientPlayer player = ClientTeamManager.INSTANCE.selfKnownPlayer;
		if (player != null && player.getExtraData() != null) {
			list.add(new TextComponent(String.format("◎ %,d", player.getExtraData().getLong("Money"))).withStyle(ChatFormatting.GOLD));
		}
	}

	@Override
	public void onClicked(MouseButton button) {
		playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}