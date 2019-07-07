package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageTogglePinned;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonPinViewQuest extends SimpleTextButton
{
	public static Icon PIN_OFF = Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/pin_off.png");
	public static Icon PIN_ON = Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/pin_on.png");

	public ButtonPinViewQuest(PanelViewQuest parent)
	{
		super(parent, I18n.format(parent.gui.file.pinnedQuests.contains(parent.quest.id) ? "ftbquests.gui.unpin" : "ftbquests.gui.pin"), parent.gui.file.pinnedQuests.contains(parent.quest.id) ? PIN_ON : PIN_OFF);
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