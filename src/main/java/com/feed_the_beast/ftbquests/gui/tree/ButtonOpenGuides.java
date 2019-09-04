package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonOpenGuides extends ButtonTab
{
	public ButtonOpenGuides(Panel panel)
	{
		super(panel, I18n.format("sidebar_button.ftbguides.guides"), Icon.getIcon("ftbguides:textures/items/book.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("ftbguides:open_gui");
	}
}