package dev.ftb.mods.ftbquests.client;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import dev.ftb.mods.ftblibrary.util.PanelPositioning;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.config.LocaleValue;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.LOCAL_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;

public interface FTBQuestsClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(FTBQuestsAPI.MOD_ID + "-client");
    String CLIENT_CONFIG = "client-config.snbt";

    SNBTConfig UI = CONFIG.addGroup("ui", 0);
    BooleanValue OLD_SCROLL_WHEEL = UI.addBoolean("old_scroll_wheel", false);
    EnumValue<PanelPositioning> PINNED_QUESTS_POS = UI.addEnum("pinned_quests_pos", PanelPositioning.NAME_MAP, PanelPositioning.RIGHT);
    IntValue PINNED_QUESTS_INSET_X = UI.addInt("pinned_quests_inset_x", 2);
    IntValue PINNED_QUESTS_INSET_Y = UI.addInt("pinned_quests_inset_y", 2);

    SNBTConfig XLATE = CONFIG.addGroup("xlate", 1);
    StringValue EDITING_LOCALE = XLATE.add(new LocaleValue(XLATE,"editing_locale", ""));
    BooleanValue HILITE_MISSING = XLATE.addBoolean("hilite_missing", true);

    // TODO migrate chapter-pinned and pinned-quests data out of per-player team data into here

    static void openSettings(Screen screen) {
        String prevLocale = EDITING_LOCALE.get();

        ConfigGroup group = new ConfigGroup("ftbquests", accepted -> {
            if (accepted) {
                saveConfig();
                if (!prevLocale.equals(EDITING_LOCALE.get()) && ClientQuestFile.INSTANCE != null) {
                    NetworkManager.sendToServer(new RequestTranslationTableMessage(ClientQuestFile.INSTANCE.getLocale()));
                    ClientQuestFile.INSTANCE.clearCachedData();
                }
            }
            Minecraft.getInstance().setScreen(screen);
        });
        CONFIG.createClientConfig(group);
        EditConfigScreen gui = new EditConfigScreen(group) {
            @Override
            public boolean doesGuiPauseGame() {
                return screen.isPauseScreen();
            }
        };

        gui.openGui();
    }

    static void init() {
        loadDefaulted(CONFIG, LOCAL_DIR.resolve(FTBQuestsAPI.MOD_ID), FTBQuestsAPI.MOD_ID, CLIENT_CONFIG);
    }

    static void saveConfig() {
        CONFIG.save(LOCAL_DIR.resolve(FTBQuestsAPI.MOD_ID).resolve(CLIENT_CONFIG));
    }
}
