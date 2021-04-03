package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.WidgetLayout;

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