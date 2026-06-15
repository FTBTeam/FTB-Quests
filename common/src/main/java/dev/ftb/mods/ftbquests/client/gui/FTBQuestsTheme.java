package dev.ftb.mods.ftbquests.client.gui;

import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
	public void drawGui(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
		IconHelper.renderIcon(ThemeProperties.BACKGROUND.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawButton(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
		if (type == WidgetType.DISABLED) {
			IconHelper.renderIcon(ThemeProperties.DISABLED_BUTTON.get(), graphics, x, y, w, h);
		} else if (type == WidgetType.MOUSE_OVER) {
			IconHelper.renderIcon(ThemeProperties.HOVER_BUTTON.get(), graphics, x, y, w, h);
		} else {
			IconHelper.renderIcon(ThemeProperties.BUTTON.get(), graphics, x, y, w, h);
		}
	}

	@Override
	public void drawContainerSlot(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		IconHelper.renderIcon(ThemeProperties.CONTAINER_SLOT.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawPanelBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		IconHelper.renderIcon(ThemeProperties.PANEL.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawContextMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		IconHelper.renderIcon(ThemeProperties.CONTEXT_MENU.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawScrollBarBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
		IconHelper.renderIcon(ThemeProperties.SCROLL_BAR_BACKGROUND.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawScrollBar(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
		IconHelper.renderIcon(ThemeProperties.SCROLL_BAR.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawTextBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		IconHelper.renderIcon(ThemeProperties.TEXT_BOX.get(), graphics, x, y, w, h);
	}

	@Override
	public void drawWidget(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
		IconHelper.renderIcon(Color4I.BLACK.withAlpha(60), graphics, x, y, w, h);
		IconHelper.renderIcon(ThemeProperties.BUTTON.get(), graphics, x, y, w, h);
	}
}
