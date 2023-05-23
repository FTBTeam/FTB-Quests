package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SyncStructuresRequestMessage extends BaseC2SMessage {
    public SyncStructuresRequestMessage() {
    }

    public SyncStructuresRequestMessage(FriendlyByteBuf buf) {
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.SYNC_STRUCTURES_REQUEST;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer sp && sp.getServer() != null) {
            new SyncStructuresResponseMessage(sp.getServer()).sendTo(sp);
        }
    }
}
