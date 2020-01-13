package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonOpenShop extends ButtonTab
{
	public ButtonOpenShop(Panel panel)
	{
		super(panel, I18n.format("sidebar_button.ftbmoney.shop"), ThemeProperties.SHOP_ICON.get());
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(getTitle());
		list.add(TextFormatting.GOLD + String.format("\u0398 %,d", ClientQuestFile.INSTANCE.self.getMoney()));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}