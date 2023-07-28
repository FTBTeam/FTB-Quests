package dev.ftb.mods.ftbquests.client.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class TabButton extends Button {
	protected final QuestScreen questScreen;

	public TabButton(Panel panel, Component title, Icon icon) {
		super(panel, title, icon);

		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 18);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		icon.draw(graphics, x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver()) {
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(questScreen.selectedChapter);
			backgroundColor.draw(graphics, x + 1, y, w - 2, h);
		}
	}
}