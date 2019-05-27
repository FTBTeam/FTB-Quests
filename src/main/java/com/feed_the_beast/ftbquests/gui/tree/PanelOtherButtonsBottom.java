package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.WidgetLayout;

/**
 * @author LatvianModder
 */
public class PanelOtherButtonsBottom extends PanelOtherButtons
{
	public PanelOtherButtonsBottom(Panel panel)
	{
		super(panel);
	}

	@Override
	public void addWidgets()
	{
		add(new ButtonSupport(this));

		if (treeGui.file.canEdit())
		{
			add(new ButtonEditSettings(this));
		}
	}

	@Override
	public void alignWidgets()
	{
		setHeight(align(WidgetLayout.VERTICAL));
		setPos(treeGui.width - width, treeGui.height - height - 1);
	}
}