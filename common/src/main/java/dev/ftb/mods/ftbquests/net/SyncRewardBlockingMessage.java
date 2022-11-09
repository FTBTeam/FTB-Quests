package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class SyncRewardBlockingMessage extends BaseS2CMessage {
    private final UUID uuid;
    private final boolean rewardsBlocked;

    public SyncRewardBlockingMessage(UUID uuid, boolean rewardsBlocked) {
        this.uuid = uuid;
        this.rewardsBlocked = rewardsBlocked;
    }

    public SyncRewardBlockingMessage(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.rewardsBlocked = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.SYNC_REWARD_BLOCKING;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeBoolean(rewardsBlocked);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        FTBQuests.NET_PROXY.syncRewardBlocking(uuid, rewardsBlocked);
    }
}
