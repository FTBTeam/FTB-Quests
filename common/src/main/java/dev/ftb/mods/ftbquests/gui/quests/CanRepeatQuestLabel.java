package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;

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
		Icons.REFRESH.draw(matrixStack, x, y, w, h);
	}
}