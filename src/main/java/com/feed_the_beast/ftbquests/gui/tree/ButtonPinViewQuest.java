package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.net.MessageTogglePinned;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonPinViewQuest extends SimpleTextButton
{
	public ButtonPinViewQuest(PanelViewQuest parent)
	{
		super(parent, I18n.format(parent.gui.file.pinnedQuests.contains(parent.quest.id) ? "ftbquests.gui.unpin" : "ftbquests.gui.pin"), parent.gui.file.pinnedQuests.contains(parent.quest.id) ? ThemeProperties.PIN_ICON_ON.get() : ThemeProperties.PIN_ICON_OFF.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new MessageTogglePinned(((PanelViewQuest) parent).quest.id).sendToServer();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}
}