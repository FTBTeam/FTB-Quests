package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Button;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.icon.Icon;

/**
 * @author LatvianModder
 */
public abstract class ButtonTab extends Button
{
	public final GuiQuestTree treeGui;

	public ButtonTab(Panel panel, String title, Icon icon)
	{
		super(panel, title, icon);
		treeGui = (GuiQuestTree) panel.getGui();
		setSize(20, 18);
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		//treeGui.borderColor.draw(x - 1, y + 1, 1, h - 2);
		//treeGui.backgroundColor.draw(x, y + 1, w, h - 2);
		icon.draw(x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver())
		{
			treeGui.backgroundColor.draw(x + 1, y, w - 2, h);
		}
	}
}