package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import me.shedaniel.architectury.platform.Platform;

/**
 * @author LatvianModder
 */
public class OtherButtonsPanelTop extends OtherButtonsPanel {
	public OtherButtonsPanelTop(Panel panel) {
		super(panel);
	}

	@Override
	public void addWidgets() {
		add(new ModpackButton(this));

		if (Platform.isModLoaded("ftbguides")) {
			add(new OpenGuidesButton(this));
		}

		if (!treeGui.file.emergencyItems.isEmpty() && (treeGui.file.self != null || treeGui.file.canEdit())) {
			add(new EmergencyItemsButton(this));
		}

		if (!ThemeProperties.WIKI_URL.get().equals("-")) {
			add(new WikiButton(this));
		}
	}

	@Override
	public void alignWidgets() {
		setPosAndSize(treeGui.width - width, 1, width, align(WidgetLayout.VERTICAL));
	}
}