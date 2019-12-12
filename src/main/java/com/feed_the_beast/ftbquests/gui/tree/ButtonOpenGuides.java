package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonOpenGuides extends ButtonTab
{
	public ButtonOpenGuides(Panel panel)
	{
		super(panel, I18n.format("sidebar_button.ftbguides.guides"), ThemeProperties.GUIDE_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick("ftbguides:open_gui");
	}
}