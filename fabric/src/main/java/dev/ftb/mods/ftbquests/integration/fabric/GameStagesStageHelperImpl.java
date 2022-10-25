package dev.ftb.mods.ftbquests.integration.fabric;

import net.minecraft.world.entity.player.Player;

/**
 * Dummy no-op implementation since there's no Gamestages on Fabric.
 * This shouldn't ever get loaded, but just in case...
 */
public class GameStagesStageHelperImpl {
    public static boolean hasStage(Player player, String stage) {
        return false;
    }

    public static void addStage(Player player, String stage) {
    }

    public static void removeStage(Player player, String stage) {
    }
}
