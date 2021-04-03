package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbguilibrary.widget.Widget;

public class ExpandChaptersButton extends Widget {
	private final QuestScreen questScreen;

	public ExpandChaptersButton(QuestScreen panel) {
		super(panel);
		questScreen = panel;
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (!questScreen.chapterPanel.expanded) {
			ChapterPanel.ARROW_COLLAPSED.draw(matrixStack, x + (w - 12) / 2, y + (h - 12) / 2, 12, 12);
		}
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (!questScreen.chapterPanel.expanded && isMouseOver() && questScreen.viewQuestPanel.quest == null) {
			questScreen.chapterPanel.setExpanded(true);
		}
	}
}