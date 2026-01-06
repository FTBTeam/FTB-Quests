package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import net.minecraft.client.gui.GuiGraphics;

public class ExpandChaptersButton extends Widget {
	private final QuestScreen questScreen;

	public ExpandChaptersButton(QuestScreen panel) {
		super(panel);
		questScreen = panel;
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (!questScreen.chapterPanel.expanded) {
			IconHelper.renderIcon(ChapterPanel.ARROW_COLLAPSED, graphics, x + (w - 12) / 2, y + (h - 12) / 2, 12, 12);
		}
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (!questScreen.chapterPanel.expanded && isMouseOver() && !questScreen.isViewingQuest()) {
			questScreen.chapterPanel.setExpanded(true);
		}
	}
}
