package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class TaskScreenConfigRequest extends BaseS2CMessage {
    private final BlockPos pos;

    public TaskScreenConfigRequest(BlockPos pos) {
        this.pos = pos;
    }

    public TaskScreenConfigRequest(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.TASK_SCREEN_CONFIG_REQ;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        FTBQuestsClient.openScreenConfigGui(pos);
    }
}
