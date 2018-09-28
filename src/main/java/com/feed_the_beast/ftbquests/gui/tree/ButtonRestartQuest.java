package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import net.minecraft.client.resources.I18n;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ButtonRestartQuest extends Widget
{
	public ButtonRestartQuest(Panel panel)
	{
		super(panel);
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		list.add(I18n.format("ftbquests.quest.can_repeat"));
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		GuiIcons.REFRESH.draw(x, y, w, h);
	}
}