package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;

public class ImageComponentWidget extends Widget {
	private final ImageComponent component;
	private final MutableComponent mutableComponent;
	private final ViewQuestPanel viewQuestPanel;
	private final int index;

	public ImageComponentWidget(ViewQuestPanel viewQuestPanel, Panel panel, ImageComponent component, int index) {
		super(panel);

		this.viewQuestPanel = viewQuestPanel;
		this.component = component;
		this.index = index;

		mutableComponent = MutableComponent.create(this.component);
		setSize(this.component.getWidth(), this.component.getHeight());
	}

	public void addMouseOverText(TooltipList list) {
		if (mutableComponent.getStyle().getHoverEvent() != null && mutableComponent.getStyle().getHoverEvent() instanceof HoverEvent.ShowText showText) {
			list.add(showText.value());
		}
	}

	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		IconHelper.renderIcon(component.getImage(), graphics, x, y, w, h);
	}

	public ImageComponent getComponent() {
		return component;
	}

	@Override
	public boolean mouseDoubleClicked(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.canEdit()) {
			viewQuestPanel.editDescLine(this, index, false, component);
			return true;
		}

		return false;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.canEdit() && button.isRight()) {
			viewQuestPanel.editDescLine(this, index, true, component);
			return true;
		}

		return false;
	}
}

