package dev.ftb.mods.ftbquests.gui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;

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
	public void drawGui(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
		ThemeProperties.BACKGROUND.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawButton(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
		if (type == WidgetType.DISABLED) {
			ThemeProperties.DISABLED_BUTTON.get().draw(graphics, x, y, w, h);
		} else if (type == WidgetType.MOUSE_OVER) {
			ThemeProperties.HOVER_BUTTON.get().draw(graphics, x, y, w, h);
		} else {
			ThemeProperties.BUTTON.get().draw(graphics, x, y, w, h);
		}
	}

	@Override
	public void drawContainerSlot(GuiGraphics graphics, int x, int y, int w, int h) {
		ThemeProperties.CONTAINER_SLOT.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawPanelBackground(GuiGraphics graphics, int x, int y, int w, int h) {
		ThemeProperties.PANEL.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawContextMenuBackground(GuiGraphics graphics, int x, int y, int w, int h) {
		ThemeProperties.CONTEXT_MENU.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawScrollBarBackground(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
		ThemeProperties.SCROLL_BAR_BACKGROUND.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawScrollBar(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
		ThemeProperties.SCROLL_BAR.get().draw(graphics, x, y, w, h);
	}

	@Override
	public void drawTextBox(GuiGraphics graphics, int x, int y, int w, int h) {
		ThemeProperties.TEXT_BOX.get().draw(graphics, x, y, w, h);
	}
}