package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

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
		if (mutableComponent.getStyle().getHoverEvent() != null && mutableComponent.getStyle().getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
			list.add(mutableComponent.getStyle().getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT));
		}
	}

	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		component.getImage().draw(graphics, x, y, w, h);
	}

	public ImageComponent getComponent() {
		return component;
	}

	@Override
	public boolean mouseDoubleClicked(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.canEdit()) {
			viewQuestPanel.editDescLine(index, false, component);
			return true;
		}

		return false;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver() && viewQuestPanel.canEdit() && button.isRight()) {
			viewQuestPanel.editDescLine(index, true, component);
			return true;
		}

		return false;
	}

	public static void openImageEditorScreen(ImageComponent component, ConfigGroup group) {
		group.add("image", new ImageConfig(), component.imageStr(), v -> component.setImage(Icon.getIcon(v)), "")
				.setNameKey("ftbquests.gui.image");
		group.addInt("width", component.getWidth(), component::setWidth, 0, 1, 1000)
				.setNameKey("ftbquests.gui.image.width");
		group.addInt("height", component.getHeight(), component::setHeight, 0, 1, 1000)
				.setNameKey("ftbquests.gui.image.height");
		group.addEnum("align", component.getAlign(), component::setAlign, ImageComponent.ImageAlign.NAME_MAP, ImageComponent.ImageAlign.CENTER)
				.setNameKey("ftbquests.gui.image.align");
		group.addBool("fit", component.isFit(), component::setFit, false)
				.setNameKey("ftbquests.gui.image.fit");

		new EditConfigScreen(group).openGui();
	}
}

