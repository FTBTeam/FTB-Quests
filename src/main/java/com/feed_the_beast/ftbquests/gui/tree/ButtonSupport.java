package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonSupport extends ButtonTab
{
	public ButtonSupport(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.subscribe"), Icon.getIcon(FTBLib.MOD_ID + ":textures/icons/twitch.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("https://www.twitch.tv/latvianmodder/subscribe");
	}
}