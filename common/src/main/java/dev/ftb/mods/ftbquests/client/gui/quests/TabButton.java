package dev.ftb.mods.ftbquests.client.gui.quests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

public abstract class TabButton extends Button {
	protected final QuestScreen questScreen;

	public TabButton(Panel panel, Component title, Icon icon) {
		super(panel, title, icon);

		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 18);
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		IconHelper.renderIcon(icon, graphics, x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver()) {
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(questScreen.selectedChapter);
			IconHelper.renderIcon(backgroundColor, graphics, x + 1, y, w - 2, h);
		}
	}
}
