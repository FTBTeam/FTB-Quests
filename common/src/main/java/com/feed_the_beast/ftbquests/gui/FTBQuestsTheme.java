package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * @author LatvianModder
 */
public class FTBQuestsTheme extends Theme {
	public static final FTBQuestsTheme INSTANCE = new FTBQuestsTheme();

	@Override
	public Color4I getContentColor(WidgetType type) {
		if (type == WidgetType.DISABLED) {
			return ThemeProperties.DISABLED_TEXT_COLOR.get();
		} else if (type == WidgetType.MOUSE_OVER) {
			return ThemeProperties.HOVER_TEXT_COLOR.get();
		}

		return ThemeProperties.TEXT_COLOR.get();
	}

	@Override
	public void drawGui(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
		ThemeProperties.BACKGROUND.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawButton(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
		if (type == WidgetType.DISABLED) {
			ThemeProperties.DISABLED_BUTTON.get().draw(matrixStack, x, y, w, h);
		} else if (type == WidgetType.MOUSE_OVER) {
			ThemeProperties.HOVER_BUTTON.get().draw(matrixStack, x, y, w, h);
		} else {
			ThemeProperties.BUTTON.get().draw(matrixStack, x, y, w, h);
		}
	}

	@Override
	public void drawContainerSlot(PoseStack matrixStack, int x, int y, int w, int h) {
		ThemeProperties.CONTAINER_SLOT.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawPanelBackground(PoseStack matrixStack, int x, int y, int w, int h) {
		ThemeProperties.PANEL.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawContextMenuBackground(PoseStack matrixStack, int x, int y, int w, int h) {
		ThemeProperties.CONTEXT_MENU.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawScrollBarBackground(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
		ThemeProperties.SCROLL_BAR_BACKGROUND.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawScrollBar(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type, boolean vertical) {
		ThemeProperties.SCROLL_BAR.get().draw(matrixStack, x, y, w, h);
	}

	@Override
	public void drawTextBox(PoseStack matrixStack, int x, int y, int w, int h) {
		ThemeProperties.TEXT_BOX.get().draw(matrixStack, x, y, w, h);
	}
}