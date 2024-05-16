package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record SyncStructuresRequestMessage() implements CustomPacketPayload {
    public static final Type<SyncStructuresRequestMessage> TYPE = new Type<>(FTBQuestsAPI.rl("sync_structures_request_message"));

    public static final SyncStructuresRequestMessage INSTANCE = new SyncStructuresRequestMessage();

    public static final StreamCodec<FriendlyByteBuf, SyncStructuresRequestMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<SyncStructuresRequestMessage> type() {
        return TYPE;
    }

    public static void handle(SyncStructuresRequestMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer sp && sp.getServer() != null) {
                NetworkManager.sendToPlayer(sp, SyncStructuresResponseMessage.create(sp.getServer()));
            }
        });
    }
}
