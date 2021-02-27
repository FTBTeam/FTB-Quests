package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;

/**
 * @author LatvianModder
 */
public class OtherButtonsPanelBottom extends OtherButtonsPanel {
	public OtherButtonsPanelBottom(Panel panel) {
		super(panel);
	}

	@Override
	public void addWidgets() {
		add(new AutopinButton(this));
		add(new SupportButton(this));

		if (treeGui.file.canEdit()) {
			add(new EditSettingsButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setHeight(align(WidgetLayout.VERTICAL));
		setPos(treeGui.width - width, treeGui.height - height - 1);
	}
}