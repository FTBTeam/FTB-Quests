package dev.ftb.mods.ftbquests.integration;

import net.minecraft.world.entity.player.Player;

public interface DocsMod {
    void openDocsPage(Player player, String path);

    enum None implements DocsMod {
        INSTANCE;

        @Override
        public void openDocsPage(Player player, String path) {

        }
    }
}
