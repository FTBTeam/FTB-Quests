package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * @author LatvianModder
 */
public class ButtonSupport extends ButtonTab
{
	public ButtonSupport(Panel panel)
	{
		super(panel, new TranslatableComponent("lat_support"), ThemeProperties.SUPPORT_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick("https://github.com/sponsors/LatvianModder");
	}
}