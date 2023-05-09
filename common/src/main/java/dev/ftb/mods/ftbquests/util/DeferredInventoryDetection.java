package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeferredInventoryDetection {
    private static final long MILLIS_IN_TICK = 50L;
    private static final Object2LongOpenHashMap<UUID> playerMap = new Object2LongOpenHashMap<>();

    public static void tick(MinecraftServer server) {
        if (!playerMap.isEmpty()) {
            Set<UUID> toRemove = new HashSet<>();
            long now = System.currentTimeMillis();

            for (UUID id : playerMap.keySet()) {
                if (now >= playerMap.getLong(id)) {
                    ServerPlayer sp = server.getPlayerList().getPlayer(id);
                    if (sp != null) {
                        FTBQuestsInventoryListener.detect(sp, ItemStack.EMPTY, 0);
                    }
                    toRemove.add(id);
                }
            }

            toRemove.forEach(playerMap::removeLong);
        }
    }

    static void scheduleInventoryCheck(ServerPlayer sp) {
        int delay = Mth.clamp(ServerQuestFile.INSTANCE.detectionDelay, 1, 200);
        playerMap.putIfAbsent(sp.getUUID(), System.currentTimeMillis() + delay * MILLIS_IN_TICK);
    }
}
