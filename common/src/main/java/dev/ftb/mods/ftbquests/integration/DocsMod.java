package dev.ftb.mods.ftbquests.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface DocsMod {
    void openDocsPage(Player player, ResourceLocation bookId, @Nullable ResourceLocation pageId, String anchor);

    enum None implements DocsMod {
        INSTANCE;

        @Override
        public void openDocsPage(Player player, ResourceLocation bookId, @Nullable ResourceLocation pageId, String anchor) {
        }
    }
}
