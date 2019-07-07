package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.SimpleTextButton;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftblib.lib.util.misc.MouseButton;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.net.MessageToggleFavorite;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonFavoriteViewQuest extends SimpleTextButton
{
	private static Icon FAV_OFF = Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/fav_off.png");
	private static Icon FAV_ON = Icon.getIcon(FTBQuests.MOD_ID + ":textures/gui/fav_on.png");

	public ButtonFavoriteViewQuest(PanelViewQuest parent)
	{
		super(parent, I18n.format("ftbquests.gui.favorite"), parent.gui.file.favoriteQuests.contains(parent.quest.id) ? FAV_ON : FAV_OFF);
	}

	@Override
	public void onClicked(MouseButton button)
	{
		GuiHelper.playClickSound();
		new MessageToggleFavorite(((PanelViewQuest) parent).quest.id).sendToServer();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}
}