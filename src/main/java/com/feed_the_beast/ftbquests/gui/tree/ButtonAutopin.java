package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageTogglePinned;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonAutopin extends ButtonTab
{
	public ButtonAutopin(Panel panel)
	{
		super(panel, I18n.format(((GuiQuestTree) panel.getGui()).file.pinnedQuests.contains(1) ? "ftbquests.gui.autopin.on" : "ftbquests.gui.autopin.off"), ((GuiQuestTree) panel.getGui()).file.pinnedQuests.contains(1) ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new MessageTogglePinned(1).sendToServer();
	}
}