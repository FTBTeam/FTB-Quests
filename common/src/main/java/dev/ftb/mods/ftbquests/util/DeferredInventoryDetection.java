package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeferredInventoryDetection {
    private static final Object2LongOpenHashMap<UUID> playerMap = new Object2LongOpenHashMap<>();
    private static Level overworld = null;

    public static void tick(MinecraftServer server) {
        Set<UUID> toRemove = new HashSet<>();

        for (UUID id : playerMap.keySet()) {
            if (playerMap.getLong(id) <= overworld(server).getGameTime()) {
                ServerPlayer sp = server.getPlayerList().getPlayer(id);
                if (sp != null) {
                    FTBQuestsInventoryListener.detect(sp, ItemStack.EMPTY, 0);
                }
                toRemove.add(id);
            }
        }

        toRemove.forEach(playerMap::removeLong);
    }

    static void scheduleInventoryCheck(ServerPlayer sp) {
        if (sp.getServer() != null) {
            int delay = Mth.clamp(ServerQuestFile.INSTANCE.detectionDelay, 1, 200);
            playerMap.putIfAbsent(sp.getUUID(), overworld(sp.getServer()).getGameTime() + delay);
        }
    }

    private static Level overworld(MinecraftServer server) {
        if (overworld == null) {
            overworld = server.getLevel(Level.OVERWORLD);
        }
        return overworld;
    }
}
