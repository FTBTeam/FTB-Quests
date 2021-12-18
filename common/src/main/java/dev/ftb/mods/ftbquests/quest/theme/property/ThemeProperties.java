package dev.ftb.mods.ftbquests.quest.theme.property;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public interface ThemeProperties {
	// Main GUI //
	IconProperty BACKGROUND = new IconProperty("background");
	StringProperty EXTRA_QUEST_SHAPES = new StringProperty("extra_quest_shapes");

	// Text Color //
	ColorProperty TEXT_COLOR = new ColorProperty("text_color");
	ColorProperty HOVER_TEXT_COLOR = new ColorProperty("hover_text_color");
	ColorProperty DISABLED_TEXT_COLOR = new ColorProperty("disabled_text_color");

	// Widgets //
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
	IconProperty CHECK_ICON = new IconProperty("check_icon", new Icon() {
		@Override
		public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
			RenderSystem.disableTexture();
			Matrix4f m = matrixStack.last().pose();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer = tesselator.getBuilder();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			float dw = w / 16F;
			float dh = h / 16F;

			Color4I out = ThemeProperties.SYMBOL_OUT.get();
			int r = out.redi();
			int g = out.greeni();
			int b = out.bluei();
			int a = out.alphai();

			buffer.vertex(m, x + dw * 0, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 3, y + dh * 5, 0).color(r, g, b, a).endVertex();

			buffer.vertex(m, x + dw * 6, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 16, y + dh * 4, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 13, y + dh * 1, 0).color(r, g, b, a).endVertex();

			Color4I in = ThemeProperties.SYMBOL_IN.get();
			r = in.redi();
			g = in.greeni();
			b = in.bluei();
			a = in.alphai();

			buffer.vertex(m, x + dw * 0 + dw, y + dh * 8, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 14 - dh, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 8 + dh, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 3, y + dh * 5 + dh, 0).color(r, g, b, a).endVertex();

			buffer.vertex(m, x + dw * 6, y + dh * 8 + dh, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 14 - dh, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 16 - dw, y + dh * 4, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 13, y + dh * 1 + dh, 0).color(r, g, b, a).endVertex();

			tesselator.end();
			GlStateManager._enableTexture();
		}

		public int hashCode() {
			return 1;
		}

		public boolean equals(Object o) {
			return o == this;
		}
	});

	IconProperty ADD_ICON = new IconProperty("add_icon", new Icon() {
		@Override
		public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
			GlStateManager._disableTexture();
			Matrix4f m = matrixStack.last().pose();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer = tesselator.getBuilder();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			float dw = w / 16F;
			float dh = h / 16F;

			Color4I out = ThemeProperties.SYMBOL_OUT.get();
			int r = out.redi();
			int g = out.greeni();
			int b = out.bluei();
			int a = out.alphai();

			buffer.vertex(m, x + dw * 6, y + dh * 2, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 6, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 10, y + dh * 14, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 10, y + dh * 2, 0).color(r, g, b, a).endVertex();

			buffer.vertex(m, x + dw * 2, y + dh * 6, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 2, y + dh * 10, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 14, y + dh * 10, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 14, y + dh * 6, 0).color(r, g, b, a).endVertex();

			Color4I in = ThemeProperties.SYMBOL_IN.get();
			r = in.redi();
			g = in.greeni();
			b = in.bluei();
			a = in.alphai();

			buffer.vertex(m, x + dw * 7, y + dh * 3, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 7, y + dh * 13, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 9, y + dh * 13, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 9, y + dh * 3, 0).color(r, g, b, a).endVertex();

			buffer.vertex(m, x + dw * 3, y + dh * 7, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 3, y + dh * 9, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 13, y + dh * 9, 0).color(r, g, b, a).endVertex();
			buffer.vertex(m, x + dw * 13, y + dh * 7, 0).color(r, g, b, a).endVertex();

			tesselator.end();
			GlStateManager._enableTexture();
		}

		public int hashCode() {
			return 1;
		}

		public boolean equals(Object o) {
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
	IconProperty MODPACK_ICON = new IconProperty("modpack_icon");
	IconProperty REWARD_TABLE_ICON = new IconProperty("reward_table_icon");
	IconProperty SHOP_ICON = new IconProperty("shop_icon");
	IconProperty COLLECT_REWARDS_ICON = new IconProperty("collect_rewards_icon");
	IconProperty DELETE_ICON = new IconProperty("delete_icon");
	IconProperty RELOAD_ICON = new IconProperty("reload_icon");
	IconProperty DOWNLOAD_ICON = new IconProperty("download_icon");
	IconProperty EDIT_ICON = new IconProperty("edit_icon");
	IconProperty MOVE_UP_ICON = new IconProperty("move_up_icon");
	IconProperty MOVE_DOWN_ICON = new IconProperty("move_down_icon");

	// Quest window //
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

	// Task specific //
	IconProperty CHECKMARK_TASK_ACTIVE = new IconProperty("checkmark_task_active");
	IconProperty CHECKMARK_TASK_INACTIVE = new IconProperty("checkmark_task_inactive");
}