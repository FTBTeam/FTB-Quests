package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public class ButtonWiki extends ButtonTab
{
	public ButtonWiki(Panel panel)
	{
		super(panel, I18n.format("ftbquests.gui.wiki"), ThemeProperties.WIKI_ICON.get());
	}

	@Override
	public void onClicked(MouseButton button)
	{
		playClickSound();
		handleClick(ThemeProperties.WIKI_URL.get());
	}
}