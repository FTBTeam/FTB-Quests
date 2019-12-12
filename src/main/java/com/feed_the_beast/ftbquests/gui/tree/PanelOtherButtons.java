package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;

/**
 * @author LatvianModder
 */
public abstract class PanelOtherButtons extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelOtherButtons(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
		setWidth(20);
	}
}