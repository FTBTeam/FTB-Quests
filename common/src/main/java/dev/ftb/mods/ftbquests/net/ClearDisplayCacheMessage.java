package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.Env;
import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.item_filtering.DisplayStacksCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ClearDisplayCacheMessage extends BaseS2CMessage {
    public ClearDisplayCacheMessage() {
    }

    public ClearDisplayCacheMessage(FriendlyByteBuf buf) {
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.CLEAR_DISPLAY_CACHE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT) {
            context.queue(() -> {
                DisplayStacksCache.clear();
                FTBQuests.LOGGER.info("cleared item display cache");
            });
        }
    }

    public static void clearForAll(MinecraftServer server) {
        if (server != null) {
            ClearDisplayCacheMessage msg = new ClearDisplayCacheMessage();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                msg.sendTo(player);
            }
        }
    }
}
