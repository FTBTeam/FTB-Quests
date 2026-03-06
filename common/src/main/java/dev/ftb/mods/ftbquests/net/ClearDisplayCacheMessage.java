package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.item_filtering.DisplayStacksCache;

import org.jspecify.annotations.Nullable;

public class ClearDisplayCacheMessage implements CustomPacketPayload {
    public static final Type<ClearDisplayCacheMessage> TYPE = new Type<>(FTBQuestsAPI.id("clear_display_cache_message"));

    private static final ClearDisplayCacheMessage INSTANCE = new ClearDisplayCacheMessage();

    public static final StreamCodec<FriendlyByteBuf, ClearDisplayCacheMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ClearDisplayCacheMessage> type() {
        return TYPE;
    }

    public static void handle(ClearDisplayCacheMessage message, NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT) {
            context.queue(() -> {
                DisplayStacksCache.clear();
                FTBQuests.LOGGER.info("cleared item display cache");
            });
        }
    }

    public static void clearForAll(@Nullable MinecraftServer server) {
        if (server != null) {
            NetworkHelper.sendToAll(server, ClearDisplayCacheMessage.INSTANCE);
        }
    }
}
