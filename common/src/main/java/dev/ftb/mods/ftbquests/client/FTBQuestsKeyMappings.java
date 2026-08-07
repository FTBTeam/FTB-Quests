package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import dev.ftb.mods.ftblibrary.util.input.Input;
import dev.ftb.mods.ftblibrary.util.input.KeyConflict;
import dev.ftb.mods.ftblibrary.util.input.KeyMappingConfig;
import dev.ftb.mods.ftblibrary.util.input.KeyModifier;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

public class FTBQuestsKeyMappings {
//    private static final String FTB_QUESTS_KEY_CATEGORY = category("keys");

    // Special case: we're maintaining backwards compatibility here so players' existing key binding
    //  for the "open quests" key doesn't get unexpectedly reset
    public static final KeyMapping KEY_QUESTS = input().createKeyMapping(new KeyMappingConfig(
            "quests", FTBQuestsAPI.MOD_ID,
            new KeyMappingConfig.TypedKey(InputConstants.Type.KEYSYM, -1),
            KeyModifier.NONE,
            null,
            KeyConflict.IN_GAME
    ));
    public static final KeyMapping KEY_CYCLE_PINNED_TRACKER = input().createKeyMapping(
            KeyMappingConfig.builder("cycle_pinned_tracker", FTBQuestsAPI.MOD_ID)
                    .conflictContext(KeyConflict.IN_GAME)
                    .build()
    );

    private static final String FTB_QUESTS_KEY_CATEGORY_GUI
            = category("gui");
    public static final KeyMapping KEY_GUI_SEARCH = guiKey("search", InputConstants.KEY_F, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_RECENTER = guiKey("recenter", InputConstants.KEY_SPACE, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ZOOM_IN = guiKey("zoom_in", InputConstants.KEY_EQUALS, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ZOOM_OUT = guiKey("zoom_out", InputConstants.KEY_MINUS, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ZOOM_RESET = guiKey("zoom_reset", InputConstants.KEY_0, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_PLAYER_PREFS = guiKey("player_prefs", InputConstants.KEY_P, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_EXT_INFO = guiKey("extended_info", InputConstants.KEY_F1, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_NEXT_CHAPTER = guiKey("next_chapter", InputConstants.KEY_TAB, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_PREV_CHAPTER = input().createKeyMapping(KeyMappingConfig.builder("prev_chapter", FTB_QUESTS_KEY_CATEGORY_GUI)
            .keyboard(InputConstants.KEY_TAB)
            .modifier(KeyModifier.SHIFT)
            .noModifierFallbackKey(InputConstants.UNKNOWN)
            .conflictContext(KeyConflict.ANY_GUI)
            .build());
    public static final KeyMapping KEY_GUI_UP = guiKey("move_up", InputConstants.KEY_UP, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_DOWN = guiKey("move_down", InputConstants.KEY_DOWN, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_LEFT = guiKey("move_left", InputConstants.KEY_LEFT, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_RIGHT = guiKey("move_right", InputConstants.KEY_RIGHT, KeyModifier.NONE);

    private static final String FTB_QUESTS_KEY_CATEGORY_GUI_EDITOR
            = category("gui_editor");
    public static final KeyMapping KEY_GUI_TOGGLE_CROSSHAIRS = guiEditorKey("toggle_crosshairs", InputConstants.KEY_R, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_SELECT_ALL = guiEditorKey("select_all", InputConstants.KEY_A, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_SELECT_NONE = guiEditorKey("select_none", InputConstants.KEY_D, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_COPY = guiEditorKey("copy", InputConstants.KEY_C, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_PASTE = guiEditorKey("paste", InputConstants.KEY_V, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_RELOAD_THEME = guiEditorKey("reload_theme", InputConstants.KEY_F5, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_DELETE = guiEditorKey("delete", InputConstants.KEY_DELETE, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_FORCE_DELETE = input().createKeyMapping(KeyMappingConfig.builder("force_delete", FTB_QUESTS_KEY_CATEGORY_GUI_EDITOR)
            .keyboard(InputConstants.KEY_DELETE)
            .modifier(KeyModifier.SHIFT)
            .noModifierFallbackKey(InputConstants.UNKNOWN)
            .conflictContext(KeyConflict.ANY_GUI)
            .build());

    public static final KeyMapping KEY_GUI_REWARD_TABLES = guiEditorKey("reward_tables", InputConstants.KEY_T, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_UNDO = guiEditorKey("undo", InputConstants.KEY_Z, KeyModifier.CONTROL);
    public static final KeyMapping KEY_GUI_REDO = guiEditorKey("redo", InputConstants.KEY_Y, KeyModifier.CONTROL);

    private static final String FTB_QUESTS_KEY_CATEGORY_GUI_QUEST_PANEL
            = category("gui_quest_panel");
    public static final KeyMapping KEY_GUI_EDIT_TITLE = guiPanelKey("edit_title", InputConstants.KEY_T, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_EDIT_SUBTITLE = guiPanelKey("edit_subtitle", InputConstants.KEY_S, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_EDIT_DESC = guiPanelKey("edit_desc", InputConstants.KEY_D, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ADD_PAGE_BREAK = guiPanelKey("add_page_break", InputConstants.KEY_P, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ADD_LINE = guiPanelKey("add_line", InputConstants.KEY_L, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_ADD_IMAGE = guiPanelKey("add_image", InputConstants.KEY_I, KeyModifier.NONE);
    public static final KeyMapping KEY_GUI_EDIT_QUEST_PROPS = guiPanelKey("edit_quest_props", InputConstants.KEY_Q, KeyModifier.NONE);

    public static final List<KeyMapping> GUI_KEYS = List.of(
            KEY_GUI_ZOOM_IN,
            KEY_GUI_ZOOM_OUT,
            KEY_GUI_ZOOM_RESET,
            KEY_GUI_NEXT_CHAPTER,
            KEY_GUI_PREV_CHAPTER,
            KEY_GUI_RECENTER,
            KEY_GUI_SEARCH,
            KEY_GUI_EXT_INFO,
            KEY_GUI_UP,
            KEY_GUI_DOWN,
            KEY_GUI_LEFT,
            KEY_GUI_RIGHT
    );

    public static final List<KeyMapping> EDITOR_KEYS = List.of(
            KEY_GUI_UNDO,
            KEY_GUI_REDO,
            KEY_GUI_SELECT_ALL,
            KEY_GUI_SELECT_NONE,
            KEY_GUI_DELETE,
            KEY_GUI_FORCE_DELETE,
            KEY_GUI_TOGGLE_CROSSHAIRS,
            KEY_GUI_COPY,
            KEY_GUI_PASTE,
            KEY_GUI_REWARD_TABLES
    );

    public static final List<KeyMapping> QUEST_PANEL_KEYS = List.of(
            KEY_GUI_EDIT_TITLE,
            KEY_GUI_EDIT_SUBTITLE,
            KEY_GUI_EDIT_DESC,
            KEY_GUI_ADD_PAGE_BREAK,
            KEY_GUI_ADD_LINE,
            KEY_GUI_ADD_IMAGE,
            KEY_GUI_EDIT_QUEST_PROPS
    );

    public static void init() {
        // in-game category
        input().registerKeyMapping(FTBQuestsAPI.MOD_ID, KEY_QUESTS);
        input().registerKeyMapping(FTBQuestsAPI.MOD_ID, KEY_CYCLE_PINNED_TRACKER);

        // gui category
        // We do this to remove the player prefs from the Key reference as it's ordered differently.
        var guiKeys = new ArrayList<>(GUI_KEYS);
        guiKeys.add(KEY_GUI_PLAYER_PREFS);

        input().registerKeyMapping(FTBQuestsAPI.MOD_ID, guiKeys.toArray(new KeyMapping[0]));

        // gui editor mode category
        // We do this to add the reload theme key to the Key reference as it's ordered differently.
        var editorKeys = new ArrayList<>(EDITOR_KEYS);
        editorKeys.add(KEY_GUI_RELOAD_THEME);

        input().registerKeyMapping(FTBQuestsAPI.MOD_ID, editorKeys.toArray(new KeyMapping[0]));

        // gui editor mode (quest panel) category
        input().registerKeyMapping(FTBQuestsAPI.MOD_ID, QUEST_PANEL_KEYS.toArray(new KeyMapping[0]));
    }

    private static KeyMapping guiKey(String name, int keyCode, KeyModifier modifier) {
        return makeKeyMapping(name, keyCode, modifier, FTB_QUESTS_KEY_CATEGORY_GUI);
    }

    private static KeyMapping guiEditorKey(String name, int keyCode, KeyModifier modifier) {
        return makeKeyMapping(name, keyCode, modifier, FTB_QUESTS_KEY_CATEGORY_GUI_EDITOR);
    }

    private static KeyMapping guiPanelKey(String name, int keyCode, KeyModifier modifier) {
        return makeKeyMapping(name, keyCode, modifier, FTB_QUESTS_KEY_CATEGORY_GUI_QUEST_PANEL);
    }

    private static KeyMapping makeKeyMapping(String name, int keyCode, KeyModifier modifier, String category) {
        return input().createKeyMapping(KeyMappingConfig.builder(name, category)
                .keyboard(keyCode)
                .modifier(modifier)
                .conflictContext(KeyConflict.ANY_GUI)
                .build());
    }

    private static Input input() {
        return ClientUtils.input();
    }

    private static String category(String suffix) {
        return "ftbquests." + suffix;
    }
}
