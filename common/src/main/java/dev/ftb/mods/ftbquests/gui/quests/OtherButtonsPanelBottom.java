package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

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

		if (questScreen.file.canEdit()) {
			add(new EditSettingsButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setHeight(align(WidgetLayout.VERTICAL));
		setPos(questScreen.width - width, questScreen.height - height - 1);
	}
}
