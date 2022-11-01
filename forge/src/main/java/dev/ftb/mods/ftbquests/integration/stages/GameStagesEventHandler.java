package dev.ftb.mods.ftbquests.integration.stages;

import dev.ftb.mods.ftbquests.quest.task.StageTask;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class GameStagesEventHandler {
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(GameStagesEventHandler::onStageAdded);
        MinecraftForge.EVENT_BUS.addListener(GameStagesEventHandler::onStageRemoved);
        MinecraftForge.EVENT_BUS.addListener(GameStagesEventHandler::onStagesCleared);
    }

    private static void onStageAdded(GameStageEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer sp) StageTask.checkStages(sp);
    }

    private static void onStageRemoved(GameStageEvent.Removed event) {
        if (event.getEntity() instanceof ServerPlayer sp) StageTask.checkStages(sp);
    }

    private static void onStagesCleared(GameStageEvent.Cleared event) {
        if (event.getEntity() instanceof ServerPlayer sp) StageTask.checkStages(sp);
    }
}
