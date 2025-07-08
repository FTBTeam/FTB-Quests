package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.ftblibrary.util.PanelPositioning;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.config.LocaleValue;
import dev.ftb.mods.ftbquests.client.gui.QuestsClientConfigScreen;

public interface FTBQuestsClientConfig {
    String KEY = FTBQuestsAPI.MOD_ID + "-client";
    SNBTConfig CONFIG = SNBTConfig.create(KEY);

    SNBTConfig UI = CONFIG.addGroup("ui", 0);
    BooleanValue OLD_SCROLL_WHEEL = UI.addBoolean("old_scroll_wheel", false);
    EnumValue<PanelPositioning> PINNED_QUESTS_POS = UI.addEnum("pinned_quests_pos", PanelPositioning.NAME_MAP, PanelPositioning.RIGHT);
    IntValue PINNED_QUESTS_INSET_X = UI.addInt("pinned_quests_inset_x", 2);
    IntValue PINNED_QUESTS_INSET_Y = UI.addInt("pinned_quests_inset_y", 2);
    BooleanValue SHOW_LOCK_ICON = UI.addBoolean("show_lock_icon", true);
    BooleanValue BACKSPACE_HISTORY = UI.addBoolean("backspace_history", true);
    BooleanValue CHAPTER_PANEL_PINNED = UI.addBoolean("chapter_panel_pinned", false);

    SNBTConfig NOTIFICATIONS = CONFIG.addGroup("notifications", 1);
    EnumValue<NotificationStyle> COMPLETION_STYLE = NOTIFICATIONS.addEnum("completion_style", NotificationStyle.NAME_MAP);
    EnumValue<NotificationStyle> REWARD_STYLE = NOTIFICATIONS.addEnum("reward_style", NotificationStyle.NAME_MAP);
    BooleanValue COMPLETION_SOUNDS = NOTIFICATIONS.addBoolean("completion_sounds", true);

    SNBTConfig XLATE = CONFIG.addGroup("xlate", 2);
    StringValue EDITING_LOCALE = XLATE.add(new LocaleValue(XLATE,"editing_locale", ""));
    BooleanValue HILITE_MISSING = XLATE.addBoolean("hilite_missing", true);

    // TODO migrate chapter-pinned and pinned-quests data out of per-player team data into here

    static void openSettings(boolean pauseGame) {
        ConfigManager.getInstance().createConfigGroup(KEY)
                .ifPresent(group -> new QuestsClientConfigScreen(group, pauseGame).openGui());
    }

    static void setChapterPanelPinned(boolean pinned) {
        if (pinned != CHAPTER_PANEL_PINNED.get()) {
            CHAPTER_PANEL_PINNED.set(pinned);
            ConfigManager.getInstance().save(KEY);
        }
    }
}
