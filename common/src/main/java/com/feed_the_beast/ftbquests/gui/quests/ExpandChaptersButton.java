package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.vertex.PoseStack;

public class ExpandChaptersButton extends Widget {
	public static final Icon ARROW_RIGHT = Icon.getIcon("ftbquests:textures/gui/arrow_right.png");
	private final QuestsScreen questsScreen;

	public ExpandChaptersButton(QuestsScreen panel) {
		super(panel);
		questsScreen = panel;
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		ARROW_RIGHT.draw(matrixStack, x + (w - 12) / 2, y + (h - 12) / 2, 12, 12);
	}

	@Override
	public void updateMouseOver(int mouseX, int mouseY) {
		super.updateMouseOver(mouseX, mouseY);

		if (!questsScreen.chapterPanel.expanded && isMouseOver()) {
			questsScreen.chapterPanel.expanded = true;
			questsScreen.chapterPanel.refreshWidgets();
		}
	}
}