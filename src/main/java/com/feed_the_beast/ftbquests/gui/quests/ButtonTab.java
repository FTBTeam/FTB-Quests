package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;

/**
 * @author LatvianModder
 */
public abstract class ButtonTab extends Button
{
	public final GuiQuests treeGui;

	public ButtonTab(Panel panel, String title, Icon icon)
	{
		super(panel, title, icon);
		treeGui = (GuiQuests) panel.getGui();
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
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(treeGui.selectedChapter);
			backgroundColor.draw(x + 1, y, w - 2, h);
		}
	}
}