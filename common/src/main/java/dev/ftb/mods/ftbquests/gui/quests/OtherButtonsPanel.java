package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.ui.Panel;

/**
 * @author LatvianModder
 */
public abstract class OtherButtonsPanel extends Panel {
	public final QuestScreen questScreen;

	public OtherButtonsPanel(Panel panel) {
		super(panel);
		questScreen = (QuestScreen) panel.getGui();
		setWidth(20);
	}
}