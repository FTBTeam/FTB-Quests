package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

/**
 * @author LatvianModder
 */
public abstract class TabButton extends Button {
	public final QuestsScreen treeGui;

	public TabButton(Panel panel, Component title, Icon icon) {
		super(panel, title, icon);
		treeGui = (QuestsScreen) panel.getGui();
		setSize(20, 18);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		//treeGui.borderColor.draw(x - 1, y + 1, 1, h - 2);
		//treeGui.backgroundColor.draw(x, y + 1, w, h - 2);
		icon.draw(matrixStack, x + (w - 16) / 2, y + (h - 16) / 2, 16, 16);

		if (isMouseOver()) {
			Color4I backgroundColor = ThemeProperties.WIDGET_BACKGROUND.get(treeGui.selectedChapter);
			backgroundColor.draw(matrixStack, x + 1, y, w - 2, h);
		}
	}
}