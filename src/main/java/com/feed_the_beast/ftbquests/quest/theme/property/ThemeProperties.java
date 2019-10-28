package com.feed_the_beast.ftbquests.quest.theme.property;

import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.events.ThemePropertyEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author LatvianModder
 */
public interface ThemeProperties
{
	//Main GUI
	IconProperty BACKGROUND = new IconProperty("background");

	//Text Color
	ColorProperty TEXT_COLOR = new ColorProperty("text_color");
	ColorProperty HOVER_TEXT_COLOR = new ColorProperty("hover_text_color");
	ColorProperty DISABLED_TEXT_COLOR = new ColorProperty("disabled_text_color");

	//Widgets
	ColorProperty WIDGET_BORDER = new ColorProperty("widget_border");
	ColorProperty WIDGET_BACKGROUND = new ColorProperty("widget_background");
	ColorProperty SYMBOL_IN = new ColorProperty("symbol_in");
	ColorProperty SYMBOL_OUT = new ColorProperty("symbol_out");

	IconProperty BUTTON = new IconProperty("button");
	IconProperty PANEL = new IconProperty("panel");
	IconProperty DISABLED_BUTTON = new IconProperty("disabled_button");
	IconProperty HOVER_BUTTON = new IconProperty("hover_button");
	IconProperty CONTEXT_MENU = new IconProperty("context_menu");
	IconProperty SCROLL_BAR_BACKGROUND = new IconProperty("scroll_bar_background");
	IconProperty SCROLL_BAR = new IconProperty("scroll_bar");
	IconProperty CONTAINER_SLOT = new IconProperty("container_slot");
	IconProperty TEXT_BOX = new IconProperty("text_box");

	//Icons
	IconProperty CHECK_ICON = new IconProperty("check_icon", new Icon()
	{
		@Override
		public void draw(int x, int y, int w, int h)
		{
			GlStateManager.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

			double dw = w / 16D;
			double dh = h / 16D;

			Color4I out = ThemeProperties.SYMBOL_OUT.get();
			int r = out.redi();
			int g = out.greeni();
			int b = out.bluei();
			int a = out.alphai();

			buffer.pos(x + dw * 0, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 3, y + dh * 5, 0).color(r, g, b, a).endVertex();

			buffer.pos(x + dw * 6, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 16, y + dh * 4, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 13, y + dh * 1, 0).color(r, g, b, a).endVertex();

			Color4I in = ThemeProperties.SYMBOL_IN.get();
			r = in.redi();
			g = in.greeni();
			b = in.bluei();
			a = in.alphai();

			buffer.pos(x + dw * 0 + dw, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 14 - dh, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 8 + dh, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 3, y + dh * 5 + dh, 0).color(r, g, b, a).endVertex();

			buffer.pos(x + dw * 6, y + dh * 8 + dh, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 14 - dh, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 16 - dw, y + dh * 4, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 13, y + dh * 1 + dh, 0).color(r, g, b, a).endVertex();

			tessellator.draw();
			GlStateManager.enableTexture2D();
		}

		public int hashCode()
		{
			return 1;
		}

		public boolean equals(Object o)
		{
			return o == this;
		}
	});

	IconProperty ADD_ICON = new IconProperty("add_icon", new Icon()
	{
		@Override
		public void draw(int x, int y, int w, int h)
		{
			GlStateManager.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

			double dw = w / 16D;
			double dh = h / 16D;

			Color4I out = ThemeProperties.SYMBOL_OUT.get();
			int r = out.redi();
			int g = out.greeni();
			int b = out.bluei();
			int a = out.alphai();

			buffer.pos(x + dw * 6, y + dh * 2, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 10, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 10, y + dh * 2, 0).color(r, g, b, a).endVertex();

			buffer.pos(x + dw * 2, y + dh * 6, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 2, y + dh * 10, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 14, y + dh * 10, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 14, y + dh * 6, 0).color(r, g, b, a).endVertex();

			Color4I in = ThemeProperties.SYMBOL_IN.get();
			r = in.redi();
			g = in.greeni();
			b = in.bluei();
			a = in.alphai();

			buffer.pos(x + dw * 7, y + dh * 3, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 7, y + dh * 13, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 9, y + dh * 13, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 9, y + dh * 3, 0).color(r, g, b, a).endVertex();

			buffer.pos(x + dw * 3, y + dh * 7, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 3, y + dh * 9, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 13, y + dh * 9, 0).color(r, g, b, a).endVertex();
			buffer.pos(x + dw * 13, y + dh * 7, 0).color(r, g, b, a).endVertex();

			tessellator.draw();
			GlStateManager.enableTexture2D();
		}

		public int hashCode()
		{
			return 1;
		}

		public boolean equals(Object o)
		{
			return o == this;
		}
	});

	IconProperty ALERT_ICON = new IconProperty("alert_icon");
	IconProperty SUPPORT_ICON = new IconProperty("support_icon");
	IconProperty WIKI_ICON = new IconProperty("wiki_icon");
	StringProperty WIKI_URL = new StringProperty("wiki_url");
	IconProperty PIN_ICON_ON = new IconProperty("pin_icon_on");
	IconProperty PIN_ICON_OFF = new IconProperty("pin_icon_off");
	IconProperty SETTINGS_ICON = new IconProperty("settings_icon");
	IconProperty CLOSE_ICON = new IconProperty("close_icon");
	IconProperty EMERGENCY_ITEMS_ICON = new IconProperty("emergency_items_icon");
	IconProperty GUIDE_ICON = new IconProperty("guide_icon");
	IconProperty COLLECT_REWARDS_ICON = new IconProperty("collect_rewards_icon");
	IconProperty REWARD_TABLE_ICON = new IconProperty("reward_table_icon");
	IconProperty SHOP_ICON = new IconProperty("shop_icon");
	IconProperty DELETE_ICON = new IconProperty("delete_icon");
	IconProperty RELOAD_ICON = new IconProperty("reload_icon");
	IconProperty DOWNLOAD_ICON = new IconProperty("download_icon");
	IconProperty EDIT_ICON = new IconProperty("edit_icon");
	IconProperty MOVE_UP_ICON = new IconProperty("move_up_icon");
	IconProperty MOVE_DOWN_ICON = new IconProperty("move_down_icon");

	//Quest window
	IconProperty ICON = new IconProperty("icon");
	IntProperty FULL_SCREEN_QUEST = new IntProperty("full_screen_quest", 0, 1);
	ColorProperty TASKS_TEXT_COLOR = new ColorProperty("tasks_text_color");
	ColorProperty REWARDS_TEXT_COLOR = new ColorProperty("rewards_text_color");
	IconProperty QUEST_VIEW_BACKGROUND = new IconProperty("quest_view_background");
	ColorProperty QUEST_VIEW_BORDER = new ColorProperty("quest_view_border");
	ColorProperty QUEST_VIEW_TITLE = new ColorProperty("quest_view_title");
	ColorProperty QUEST_COMPLETED_COLOR = new ColorProperty("quest_completed_color");
	ColorProperty QUEST_STARTED_COLOR = new ColorProperty("quest_started_color");
	IconProperty DEPENDENCY_LINE_TEXTURE = new IconProperty("dependency_line_texture");
	ColorProperty DEPENDENCY_LINE_COMPLETED_COLOR = new ColorProperty("dependency_line_completed_color");
	ColorProperty DEPENDENCY_LINE_REQUIRES_COLOR = new ColorProperty("dependency_line_requires_color");
	ColorProperty DEPENDENCY_LINE_REQUIRED_FOR_COLOR = new ColorProperty("dependency_line_required_for_color");
	DoubleProperty DEPENDENCY_LINE_SELECTED_SPEED = new DoubleProperty("dependency_line_selected_speed", 0D, 1000D);
	DoubleProperty DEPENDENCY_LINE_UNSELECTED_SPEED = new DoubleProperty("dependency_line_unselected_speed", 0D, 1000D);
	DoubleProperty DEPENDENCY_LINE_THICKNESS = new DoubleProperty("dependency_line_thickness", 0D, 3D);
	DoubleProperty QUEST_SPACING = new DoubleProperty("quest_spacing", 0D, 8D);
	DoubleProperty PINNED_QUEST_SIZE = new DoubleProperty("pinned_quest_size", 0D, 3D);

	static void register(ThemePropertyEvent event)
	{
		try
		{
			for (Field field : ThemeProperties.class.getDeclaredFields())
			{
				if (Modifier.isStatic(field.getModifiers()) && ThemeProperty.class.isAssignableFrom(field.getType()))
				{
					field.setAccessible(true);
					event.register((ThemeProperty) field.get(null));
				}
			}
		}
		catch (Exception ex)
		{
		}
	}
}