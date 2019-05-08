package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;

/**
 * @author LatvianModder
 */
public class ButtonWiki extends ButtonTab
{
	public ButtonWiki(Panel panel)
	{
		super(panel, "Wiki", Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/info.png"));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		handleClick("https://minecraft.curseforge.com/projects/ftb-quests/pages");
	}
}