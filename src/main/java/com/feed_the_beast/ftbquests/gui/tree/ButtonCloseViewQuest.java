package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonCloseViewQuest extends SimpleTextButton
{
	public ButtonCloseViewQuest(PanelViewQuest parent)
	{
		super(parent, I18n.format("gui.close"), ThemeProperties.CLOSE_ICON.get(parent.quest));
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		((GuiQuestTree) getGui()).closeQuest();
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}
}