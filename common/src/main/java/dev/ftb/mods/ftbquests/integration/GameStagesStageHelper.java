package dev.ftb.mods.ftbquests.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class GameStagesStageHelper extends StageHelper {
    @Override
    public boolean has(Player player, String stage) {
        return hasStage(player, stage);
    }

    @Override
    public void add(ServerPlayer player, String stage) {
        addStage(player, stage);
    }

    @Override
    public void remove(ServerPlayer player, String stage) {
        removeStage(player, stage);
    }

    @ExpectPlatform
    public static boolean hasStage(Player player, String stage) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addStage(Player player, String stage) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void removeStage(Player player, String stage) {
        throw new AssertionError();
    }
}
