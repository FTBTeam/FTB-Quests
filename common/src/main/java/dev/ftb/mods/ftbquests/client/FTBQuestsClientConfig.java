package dev.ftb.mods.ftbquests.client;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.LOCAL_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;

public interface FTBQuestsClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(FTBQuestsAPI.MOD_ID + "-client");
    String CLIENT_CONFIG = "client-config.snbt";

    SNBTConfig UI = CONFIG.addGroup("ui", 0);
    BooleanValue OLD_SCROLL_WHEEL = UI.addBoolean("old_scroll_wheel", false);

    // TODO migrate chapter-pinned and pinned-quests data out of per-player team data into here

    static void openSettings(Screen screen) {
        ConfigGroup group = new ConfigGroup("ftbquests", accepted -> {
            if (accepted) {
                saveConfig();
            }
            Minecraft.getInstance().setScreen(screen);
        });
        CONFIG.createClientConfig(group);
        EditConfigScreen gui = new EditConfigScreen(group);

        gui.openGui();
    }

    static void init() {
        loadDefaulted(CONFIG, LOCAL_DIR.resolve(FTBQuestsAPI.MOD_ID), FTBQuestsAPI.MOD_ID, CLIENT_CONFIG);
    }

    static void saveConfig() {
        CONFIG.save(LOCAL_DIR.resolve(FTBQuestsAPI.MOD_ID).resolve(CLIENT_CONFIG));
    }
}
