package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class ButtonOpenShop extends ButtonTab
{
	public ButtonOpenShop(Panel panel)
	{
		super(panel, new TranslatableComponent("sidebar_button.ftbmoney.shop"), ThemeProperties.SHOP_ICON.get());
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
		list.add(getTitle());
		list.add(new TextComponent(String.format("\u0398 %,d", ClientQuestFile.INSTANCE.self.getMoney())).withStyle(ChatFormatting.GOLD));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}