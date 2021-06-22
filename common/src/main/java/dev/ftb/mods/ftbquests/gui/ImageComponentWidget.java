package dev.ftb.mods.ftbquests.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.ImageComponent;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.gui.quests.ViewQuestPanel;
import net.minecraft.network.chat.HoverEvent;

public class ImageComponentWidget extends Widget {
	public final ImageComponent component;
	public final ViewQuestPanel viewQuestPanel;
	public final int index;

	public ImageComponentWidget(ViewQuestPanel q, Panel panel, ImageComponent c, int i) {
		super(panel);
		viewQuestPanel = q;
		component = c;
		setSize(component.width, component.height);
		index = i;
	}

	public void addMouseOverText(TooltipList list) {
		if (component.getStyle().getHoverEvent() != null && component.getStyle().getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
			list.add(component.getStyle().getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT));
		}
	}

	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		component.image.draw(matrixStack, x, y, w, h);
	}

	@Override
	public boolean mouseDoubleClicked(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.quest.getQuestFile().canEdit()) {
			viewQuestPanel.editDescLine(index, false, component);
			return true;
		}

		return false;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.quest.getQuestFile().canEdit() && button.isRight()) {
			viewQuestPanel.editDescLine(index, true, component);
			return true;
		}

		return false;
	}
}

