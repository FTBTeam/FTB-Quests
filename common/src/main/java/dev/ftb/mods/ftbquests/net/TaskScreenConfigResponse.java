package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import dev.ftb.mods.ftbteams.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.TeamRank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;

public class TaskScreenConfigResponse extends BaseC2SMessage {
    private final BlockPos pos;
    private final CompoundTag payload;

    public TaskScreenConfigResponse(TaskScreenBlockEntity taskScreenBlockEntity) {
        this.pos = taskScreenBlockEntity.getBlockPos();
        this.payload = taskScreenBlockEntity.saveWithoutMetadata();
    }

    public TaskScreenConfigResponse(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.payload = buf.readNbt();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.TASK_SCREEN_CONFIG_RESP;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(payload);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer().getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.getBlockEntity(pos) instanceof TaskScreenBlockEntity taskScreen) {
                TeamRank rank = FTBTeamsAPI.getHighestRank(taskScreen.getTeamId(), context.getPlayer().getUUID());
                if (rank.isMember()) {
                    taskScreen.load(payload);
                    taskScreen.setChanged();
                }
            }
        }
    }
}
