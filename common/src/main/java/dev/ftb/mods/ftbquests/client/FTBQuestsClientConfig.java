package dev.ftb.mods.ftbquests.client;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public interface FTBQuestsClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(FTBQuestsAPI.MOD_ID + "-client");

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

    static void saveConfig() {
        CONFIG.save(Platform.getGameFolder().resolve("local/ftbquests/client-config.snbt"));
    }
}
