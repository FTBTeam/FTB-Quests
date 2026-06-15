package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ExpandChaptersButton extends Widget {
	private final QuestScreen questScreen;

	public ExpandChaptersButton(QuestScreen panel) {
		super(panel);
		questScreen = panel;
	}

	@Override
	public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
		if (!questScreen.chapterPanel.isExpanded()) {
			IconHelper.renderIcon(ChapterPanel.ARROW_COLLAPSED, graphics, x + (w - 12) / 2, y + (h - 12) / 2, 12, 12);
		}
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (!questScreen.chapterPanel.isExpanded() && isMouseOver() && !questScreen.isViewingQuest()) {
			questScreen.chapterPanel.setExpanded(true);
		}
	}
}
