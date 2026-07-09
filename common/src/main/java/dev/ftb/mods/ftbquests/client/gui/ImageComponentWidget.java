package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.ImageClickAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
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
		if (mutableComponent.getStyle().getHoverEvent() != null && mutableComponent.getStyle().getHoverEvent() instanceof HoverEvent.ShowText(Component value)) {
			list.add(value);
		}
	}

	public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
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
        if (isMouseOver()) {
            if (viewQuestPanel.canEdit() && button.isRight() || Minecraft.getInstance().hasAltDown() && button.isLeft()) {
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

	public static EditableConfigGroup makeEditGroup(ImageComponent component, ConfigCallback callback) {
		EditableConfigGroup config = new EditableConfigGroup(FTBQuestsAPI.MOD_ID + ".chapter.image", callback);

		ImageClickAction clickAction = ImageClickAction.fromString(component.getClickAction());

		config.add("image", new EditableImageResource(), EditableImageResource.getIdentifier(component.getImage()),
				v -> component.setImage(Icon.getIcon(v)), EditableImageResource.NONE);
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

