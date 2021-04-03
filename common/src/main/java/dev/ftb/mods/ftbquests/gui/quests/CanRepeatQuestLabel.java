package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import dev.ftb.mods.ftbguilibrary.widget.GuiIcons;
import dev.ftb.mods.ftbguilibrary.widget.Panel;
import dev.ftb.mods.ftbguilibrary.widget.Theme;
import dev.ftb.mods.ftbguilibrary.widget.Widget;

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