package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftbquests.FTBQuests;

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

		if (FTBQuests.PROXY.getClientPlayer().hasPermissions(2)) {
			// note: single player owner can't use the GUI button but can use the /ftbquests editing_mode command
			// this is intentional, since there should not be an obvious "cheat" button for single player questing
			add(new ToggleEditModeButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setHeight(align(WidgetLayout.VERTICAL));
		setPos(questScreen.width - width, questScreen.height - height - 1);
	}
}
