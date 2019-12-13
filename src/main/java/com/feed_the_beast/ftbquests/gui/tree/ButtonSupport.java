package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
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
		playClickSound();
		handleClick("https://github.com/sponsors/LatvianModder");
	}
}