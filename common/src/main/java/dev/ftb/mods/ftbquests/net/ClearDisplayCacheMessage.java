package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.item_filtering.DisplayStacksCache;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ClearDisplayCacheMessage implements CustomPacketPayload {
    public static final Type<ClearDisplayCacheMessage> TYPE = new Type<>(FTBQuestsAPI.id("clear_display_cache_message"));

    private static final ClearDisplayCacheMessage INSTANCE = new ClearDisplayCacheMessage();

    public static final StreamCodec<FriendlyByteBuf, ClearDisplayCacheMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ClearDisplayCacheMessage> type() {
        return TYPE;
    }

    public static void handle(ClearDisplayCacheMessage message, PacketContext context) {
        DisplayStacksCache.clear();
        FTBQuests.LOGGER.info("cleared item display cache");
    }

    public static void clearForAll() {
        if (ServerQuestFile.exists()) {
            Server2PlayNetworking.sendToAllPlayers(ServerQuestFile.getInstance().server, ClearDisplayCacheMessage.INSTANCE);
        }
    }
}
