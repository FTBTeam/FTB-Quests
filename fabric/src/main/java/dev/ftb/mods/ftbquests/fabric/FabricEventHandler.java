package dev.ftb.mods.ftbquests.fabric;

import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.api.fabric.FTBQuestsEvents;
import dev.ftb.mods.ftbteams.api.fabric.FTBTeamsEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricEventHandler {
    public static void init(FTBQuestsEventHandler handler) {
        ServerLifecycleEvents.SERVER_STARTING.register(handler::serverAboutToStart);
        ServerLifecycleEvents.SERVER_STARTED.register(handler::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(handler::serverStopped);
        CommandRegistrationCallback.EVENT.register(handler::registerCommands);
        ServerLifecycleEvents.AFTER_SAVE.register((_, _, _) -> handler.onWorldSaved());

        FTBQuestsEvents.CLEAR_FILE_CACHE.register(handler::onFileCacheClear);

        FTBTeamsEvents.TEAM_PLAYER_LOGGED_IN.register(handler::playerLoggedIn);
        FTBTeamsEvents.TEAM_CREATED.register(handler::teamCreated);
        FTBTeamsEvents.PLAYER_CHANGED_TEAM.register(handler::playerChangedTeam);

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((_, _, killedEntity, damageSource)
                -> handler.onPlayerKilledEntity(killedEntity, damageSource));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(handler::onPlayerTick);
            handler.onServerTickPost(server);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(handler::onPlayerRespawn);

        // See ResultSlotMixin, FurnaceResultSlotMixin, ServerLevelMixin, ServerPlayerMixin for other "event" handlers

        // TODO convert this one
//        bus.addListener(NeoEventHandler::livingDrops);
    }
}
