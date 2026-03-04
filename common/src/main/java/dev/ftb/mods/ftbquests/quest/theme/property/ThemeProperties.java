package dev.ftb.mods.ftbquests.quest.theme.property;

public interface ThemeProperties {
	// Main GUI //
	IconProperty BACKGROUND = new IconProperty("background");
	IconProperty CHAPTER_PANEL_BACKGROUND = new IconProperty("chapter_panel_background");
	IconProperty KEY_REFERENCE_BACKGROUND = new IconProperty("key_reference_background");
	StringProperty EXTRA_QUEST_SHAPES = new StringProperty("extra_quest_shapes");
	ColorProperty SELECTED_HILITE_1 = new ColorProperty("selected_chapter_highlight_1");
	ColorProperty SELECTED_HILITE_2 = new ColorProperty("selected_chapter_highlight_2");

	// Text Color //
	ColorProperty TEXT_COLOR = new ColorProperty("text_color");
	ColorProperty HOVER_TEXT_COLOR = new ColorProperty("hover_text_color");
	ColorProperty DISABLED_TEXT_COLOR = new ColorProperty("disabled_text_color");

	// Widgets //
	ColorProperty WIDGET_BORDER = new ColorProperty("widget_border");
	ColorProperty WIDGET_BACKGROUND = new ColorProperty("widget_background");

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
	IconProperty CHECK_ICON = new IconProperty("check_icon");
	IconProperty CHECK_ICON_GRAY = new IconProperty("check_icon_gray");
	IconProperty ADD_ICON = new IconProperty("add_icon");

	IconProperty ALERT_ICON = new IconProperty("alert_icon");
	IconProperty WIKI_ICON = new IconProperty("wiki_icon");
	StringProperty WIKI_URL = new StringProperty("wiki_url");
	IconProperty PIN_ICON_ON = new IconProperty("pin_icon_on");
	IconProperty PIN_ICON_OFF = new IconProperty("pin_icon_off");
	IconProperty EDITOR_ICON_ON = new IconProperty("editor_icon_on");
	IconProperty EDITOR_ICON_OFF = new IconProperty("editor_icon_off");
	IconProperty HIDDEN_ICON = new IconProperty("hidden_icon");
	IconProperty LINK_ICON = new IconProperty("link_icon");
	IconProperty SAVE_ICON = new IconProperty("save_icon");
	IconProperty SETTINGS_ICON = new IconProperty("settings_icon");
	IconProperty PREFS_ICON = new IconProperty("prefs_icon");
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
	IconProperty LOCK_ICON = new IconProperty("lock_icon");

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
	ColorProperty QUEST_NOT_STARTED_COLOR = new ColorProperty("quest_not_started_color");
	ColorProperty QUEST_LOCKED_COLOR = new ColorProperty("quest_locked_color");
	IconProperty DEPENDENCY_LINE_TEXTURE = new IconProperty("dependency_line_texture");
	ColorProperty DEPENDENCY_LINE_COMPLETED_COLOR = new ColorProperty("dependency_line_completed_color");
	ColorProperty DEPENDENCY_LINE_UNCOMPLETED_COLOR = new ColorProperty("dependency_line_uncompleted_color");
	ColorProperty DEPENDENCY_LINE_UNAVAILABLE_COLOR = new ColorProperty("dependency_line_unavailable_color");
	ColorProperty DEPENDENCY_LINE_REQUIRES_COLOR = new ColorProperty("dependency_line_requires_color");
	ColorProperty DEPENDENCY_LINE_REQUIRED_FOR_COLOR = new ColorProperty("dependency_line_required_for_color");
	DoubleProperty DEPENDENCY_LINE_SELECTED_SPEED = new DoubleProperty("dependency_line_selected_speed", 0D, 1000D);
	DoubleProperty DEPENDENCY_LINE_UNSELECTED_SPEED = new DoubleProperty("dependency_line_unselected_speed", 0D, 1000D);
	DoubleProperty DEPENDENCY_LINE_THICKNESS = new DoubleProperty("dependency_line_thickness", 0D, 3D);
	DoubleProperty QUEST_SPACING = new DoubleProperty("quest_spacing", 0D, 8D);
	IconProperty LEFT_ARROW = new IconProperty("left_arrow");
	IconProperty RIGHT_ARROW = new IconProperty("right_arrow");

	// Task specific //
	IconProperty CHECKMARK_TASK_ACTIVE = new IconProperty("checkmark_task_active");
	IconProperty CHECKMARK_TASK_INACTIVE = new IconProperty("checkmark_task_inactive");
}
