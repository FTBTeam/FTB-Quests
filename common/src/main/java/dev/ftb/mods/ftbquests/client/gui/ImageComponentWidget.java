package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.ImageClickAction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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
			viewQuestPanel.editDescLine(this, index, false, component);
			return true;
		}

		return false;
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (viewQuestPanel.canEdit() && button.isRight() || Screen.hasAltDown() && button.isLeft()) {
				viewQuestPanel.editDescLine(this, index, button.isRight(), component);
				return true;
			} else if (button.isLeft()) {
				ImageClickAction clickAction = ImageClickAction.fromString(component.getClickAction());
				clickAction.run();
				return !clickAction.isNone();
			}
		}

		return false;
	}

	public static ConfigGroup makeEditGroup(ImageComponent component, ConfigCallback callback) {
		ConfigGroup config = new ConfigGroup(FTBQuestsAPI.MOD_ID + ".image", callback);

		ImageClickAction clickAction = ImageClickAction.fromString(component.getClickAction());

		config.add("image", new ImageResourceConfig(), ImageResourceConfig.getResourceLocation(component.getImage()),
				v -> component.setImage(Icon.getIcon(v)), ImageResourceConfig.NONE);
		config.addInt("width", component.getWidth(), component::setWidth, 0, 1, 1000);
		config.addInt("height", component.getHeight(), component::setHeight, 0, 1, 1000);
		config.addEnum("align", component.getAlign(), component::setAlign, ImageComponent.ImageAlign.NAME_MAP, ImageComponent.ImageAlign.CENTER);
		config.addBool("fit", component.isFit(), component::setFit, false);
		config.addEnum("click_action_type", clickAction.actionType(), v -> updateClickType(component, v), ImageClickAction.ActionType.NAME_MAP);
		config.addString("click_action_data", clickAction.actionData(), v -> updateClickData(component, v), "");

		return config;
	}

	private static void updateClickType(ImageComponent component, ImageClickAction.ActionType type) {
		ImageClickAction clickAction = ImageClickAction.fromString(component.getClickAction());
		component.setClickAction(clickAction.withType(type).toString());
	}

	private static void updateClickData(ImageComponent component, String data) {
		ImageClickAction clickAction = ImageClickAction.fromString(component.getClickAction());
		component.setClickAction(clickAction.withData(data).toString());
	}
}

