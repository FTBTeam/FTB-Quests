package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.util.NBTUtils;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.Minecraft;
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
		list.add(TextFormatting.GOLD + String.format("\u0398 %,d", NBTUtils.getPersistedData(Minecraft.getMinecraft().player, false).getLong("ftb_money")));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("custom:ftbmoney:open_gui");
	}
}