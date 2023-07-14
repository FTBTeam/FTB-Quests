package dev.ftb.mods.ftbquests.gui.quests;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;

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
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		Icons.REFRESH.draw(graphics, x, y, w, h);
	}
}