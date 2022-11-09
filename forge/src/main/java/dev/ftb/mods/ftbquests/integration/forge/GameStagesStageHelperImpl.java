package dev.ftb.mods.ftbquests.integration.forge;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class GameStagesStageHelperImpl {
    public static boolean hasStage(Player player, String stage) {
        return GameStageHelper.hasStage(player, stage);
    }

    public static void addStage(Player player, String stage) {
        if (player instanceof ServerPlayer sp) GameStageHelper.addStage(sp, stage);
    }

    public static void removeStage(Player player, String stage) {
        if (player instanceof ServerPlayer sp) GameStageHelper.removeStage(sp, stage);
    }
}
