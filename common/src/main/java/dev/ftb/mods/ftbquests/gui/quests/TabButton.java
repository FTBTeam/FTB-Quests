package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.network.chat.Component;

public abstract class TabButton extends Button {
	public final QuestScreen questScreen;

	public TabButton(Panel panel, Component title, Icon icon) {
		super(panel, title, icon);
		questScreen = (QuestScreen) panel.getGui();
		setSize(20, 18);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		//treeGui.borderColor.draw(x - 1, y + 1, 1, h - 2);
		//treeGui.backgroundColor.draw(x, y + 1, w, h - 2);
		icon.draw(matrixStack, x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver()) {
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(questScreen.selectedChapter);
			backgroundColor.draw(matrixStack, x + 1, y, w - 2, h);
		}
	}
}
