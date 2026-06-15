package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record RequestTeamDataMessage() implements CustomPacketPayload {
    public static final Type<RequestTeamDataMessage> TYPE = new Type<>(FTBQuestsAPI.id("request_team_data_message"));

    public static final RequestTeamDataMessage INSTANCE = new RequestTeamDataMessage();

    public static final StreamCodec<FriendlyByteBuf, RequestTeamDataMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<RequestTeamDataMessage> type() {
        return TYPE;
    }

    public static void handle(RequestTeamDataMessage ignoredMessage, PacketContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            ServerQuestFile.getInstance().getTeamData(serverPlayer)
                    .ifPresent(data -> Server2PlayNetworking.send(serverPlayer, new SyncTeamDataMessage(data)));
        }
    }
}
