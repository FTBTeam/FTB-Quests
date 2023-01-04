package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

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
        if (context.getPlayer() instanceof ServerPlayer serverPlayer
                && serverPlayer.getLevel().getBlockEntity(pos) instanceof TaskScreenBlockEntity taskScreen
                && TaskScreenBlock.hasPermissionToEdit(serverPlayer, taskScreen)) {
            taskScreen.load(payload);
            serverPlayer.getLevel().sendBlockUpdated(taskScreen.getBlockPos(), taskScreen.getBlockState(), taskScreen.getBlockState(), Block.UPDATE_ALL);
            taskScreen.setChanged();
        }
    }
}
