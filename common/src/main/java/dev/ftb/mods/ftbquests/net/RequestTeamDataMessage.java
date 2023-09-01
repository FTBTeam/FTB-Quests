package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RequestTeamDataMessage extends BaseC2SMessage {
    public RequestTeamDataMessage(FriendlyByteBuf buf) {
    }

    public RequestTeamDataMessage() {
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.REQUEST_TEAM_DATA;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            TeamData data = TeamData.get(serverPlayer);
            if (data != null) {
                new SyncTeamDataMessage(data, true).sendTo(serverPlayer);
            }
        }
    }
}
