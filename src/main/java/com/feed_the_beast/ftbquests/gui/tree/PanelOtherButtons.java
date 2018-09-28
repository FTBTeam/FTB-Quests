package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;

/**
 * @author LatvianModder
 */
public class PanelOtherButtons extends Panel
{
	public final GuiQuestTree treeGui;

	public PanelOtherButtons(Panel panel)
	{
		super(panel);
		treeGui = (GuiQuestTree) panel.getGui();
	}

	@Override
	public void addWidgets()
	{
		if (!treeGui.questFile.emergencyItems.isEmpty() && (treeGui.questFile.self != null || treeGui.questFile.canEdit()))
		{
			add(new ButtonEmergencyItems(this));
		}

		add(new ButtonWiki(this));

		if (treeGui.questFile.canEdit())
		{
			add(new ButtonEditSettings(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setSize(align(new WidgetLayout.Horizontal(1, 1, 0)), treeGui.chapterPanel.height);
		setX(getGui().width - width - 1);
	}
}