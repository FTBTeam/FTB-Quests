package com.feed_the_beast.ftbquests.gui.quests;

import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * @author LatvianModder
 */
public class CanRepeatQuestLabel extends Widget {
	public CanRepeatQuestLabel(Panel panel) {
		super(panel);
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		list.translate("ftbquests.quest.can_repeat");
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		GuiIcons.REFRESH.draw(matrixStack, x, y, w, h);
	}
}