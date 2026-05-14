package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import dev.ftb.mods.ftblibrary.platform.client.keys.KeyMappingConfig;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

public class FTBQuestKeys {
    private static final List<KeyMapping> KEYS = new ArrayList<>();

    static final KeyMapping.Category FTB_QUESTS_KEY_CATEGORY = new KeyMapping.Category(FTBQuestsAPI.id("keys"));

    public static final KeyMapping KEY_QUESTS = register(KeyMappingConfig.builder("quests", FTB_QUESTS_KEY_CATEGORY)
                .build());

    static final KeyMapping.Category QUESTS_EDITOR_KEY_CATEGORY = new KeyMapping.Category(FTBQuestsAPI.id("editor"));

    public static final KeyMapping NEXT_CHAPTER = register(KeyMappingConfig.builder("next_chapter", QUESTS_EDITOR_KEY_CATEGORY)
            .keyboard(InputConstants.KEY_TAB)
            .build());

    public static final KeyMapping RESET_SCROLL = register(KeyMappingConfig.builder("reset_scroll", QUESTS_EDITOR_KEY_CATEGORY)
            .keyboard(InputConstants.KEY_SPACE)
            .build());

    public static final KeyMapping TOGGLE_GRID = register(KeyMappingConfig.builder("toggle_grid", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_R)
            .build());

    public static final KeyMapping SEARCH = register(KeyMappingConfig.builder("search", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_F)
            .build());

    public static final KeyMapping OPEN_SETTINGS = register(KeyMappingConfig.builder("open_settings", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_P)
            .build());

    public static final KeyMapping OPEN_REWARDS_TABLE = register(KeyMappingConfig.builder("open_rewards_table", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_T)
            .build());

    public static final KeyMapping RESET_ZOOM = register(KeyMappingConfig.builder("reset_zoom", QUESTS_EDITOR_KEY_CATEGORY)
            .keyboard(InputConstants.KEY_0)
            .build());

    public static final KeyMapping DELETE = register(KeyMappingConfig.builder("delete", QUESTS_EDITOR_KEY_CATEGORY)
            .keyboard(InputConstants.KEY_DELETE)
            .build());

    public static final KeyMapping DESELECT = register(KeyMappingConfig.builder("deselect", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_D)
            .build());

    public static final KeyMapping SELECT_ALL = register(KeyMappingConfig.builder("select_all", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_A)
            .build());

    public static final KeyMapping COPY = register(KeyMappingConfig.builder("copy", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_C)
            .build());

    public static final KeyMapping PASTE = register(KeyMappingConfig.builder("paste", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_V)
            .build());

    public static final KeyMapping PASTE_AS_LINK = register(KeyMappingConfig.builder("paste_as_link", QUESTS_EDITOR_KEY_CATEGORY)
            .alt().keyboard(InputConstants.KEY_V)
            .noModifierFallbackKeyboard(InputConstants.KEY_B)
            .build());

    public static final KeyMapping MOVE_UP = register(KeyMappingConfig.builder("move_up", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_UP)
            .build());

    public static final KeyMapping MOVE_DOWN = register(KeyMappingConfig.builder("move_down", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_DOWN)
            .build());

    public static final KeyMapping MOVE_LEFT = register(KeyMappingConfig.builder("move_left", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_LEFT)
            .build());

    public static final KeyMapping MOVE_RIGHT = register(KeyMappingConfig.builder("move_right", QUESTS_EDITOR_KEY_CATEGORY)
            .control().keyboard(InputConstants.KEY_RIGHT)
            .build());

    public static final KeyMapping RELOAD_THEME = register(KeyMappingConfig.builder("reload_theme", QUESTS_EDITOR_KEY_CATEGORY)
            .keyboard(InputConstants.KEY_F5)
            .build());

    public static void init() {
        PlatformClient.get().registerKeyMapping(FTBQuestsAPI.MOD_ID, KEYS.toArray(new KeyMapping[0]));
    }

    private static KeyMapping register(KeyMappingConfig config) {
        KeyMapping keyBinding = PlatformClient.get().createKeyBinding(config);
        KEYS.add(keyBinding);
        return keyBinding;
    }
}
