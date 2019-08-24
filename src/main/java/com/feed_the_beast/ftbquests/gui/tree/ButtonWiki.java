package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonWiki extends ButtonTab
{
	public ButtonWiki(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.wiki"), Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/info.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("https://www.curseforge.com/minecraft/mc-mods/ftb-quests/pages/about");
	}
}