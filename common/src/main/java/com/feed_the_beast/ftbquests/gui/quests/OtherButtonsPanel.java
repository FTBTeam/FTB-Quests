package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;

/**
 * @author LatvianModder
 */
public abstract class OtherButtonsPanel extends Panel {
	public final QuestsScreen treeGui;

	public OtherButtonsPanel(Panel panel) {
		super(panel);
		treeGui = (QuestsScreen) panel.getGui();
		setWidth(20);
	}
}