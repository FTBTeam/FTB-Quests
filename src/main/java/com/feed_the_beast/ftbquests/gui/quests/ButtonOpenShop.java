package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author LatvianModder
 */
public class ButtonOpenShop extends ButtonTab
{
	public ButtonOpenShop(Panel panel)
	{
		super(panel, new TranslationTextComponent("sidebar_button.ftbmoney.shop"), ThemeProperties.SHOP_ICON.get());
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
		list.add(getTitle());
		list.add(new StringTextComponent(String.format("\u0398 %,d", ClientQuestFile.INSTANCE.self.getMoney())).mergeStyle(TextFormatting.GOLD));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}