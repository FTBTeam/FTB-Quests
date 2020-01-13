package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextField;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;

/**
 * @author LatvianModder
 */
public class TextFieldDisabledButton extends TextField
{
	public TextFieldDisabledButton(Panel panel, String text)
	{
		super(panel);
		addFlags(Theme.CENTERED | Theme.CENTERED_V);
		setText(text);
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}
}