package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.ItemIcon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;

/**
 * @author LatvianModder
 */
public class ButtonOpenGuides extends ButtonTab
{
	public ButtonOpenGuides(Panel panel)
	{
		super(panel, I18n.format("sidebar_button.ftbguides.guides"), ItemIcon.getItemIcon(Items.BOOK));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("ftbguides:open_gui");
	}
}