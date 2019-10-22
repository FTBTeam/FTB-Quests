package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonSupport extends ButtonTab
{
	public ButtonSupport(Panel panel)
	{
		super(panel, I18n.format("lat_support"), ThemeProperties.SUPPORT_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("https://latvian.dev/supporting/");
	}
}