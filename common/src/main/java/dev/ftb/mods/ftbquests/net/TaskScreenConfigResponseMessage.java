package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.TaskScreenBlock;
import dev.ftb.mods.ftbquests.block.entity.TaskScreenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public record TaskScreenConfigResponseMessage(BlockPos pos, CompoundTag payload) implements CustomPacketPayload {
    public static final Type<TaskScreenConfigResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("task_screen_config_response_message"));

    public static final StreamCodec<FriendlyByteBuf, TaskScreenConfigResponseMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TaskScreenConfigResponseMessage::pos,
            ByteBufCodecs.COMPOUND_TAG, TaskScreenConfigResponseMessage::payload,
            TaskScreenConfigResponseMessage::new
    );

    @Override
    public Type<TaskScreenConfigResponseMessage> type() {
        return TYPE;
    }

    public static void handle(TaskScreenConfigResponseMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getBlockEntity(message.pos) instanceof TaskScreenBlockEntity taskScreen
                    && TaskScreenBlock.hasPermissionToEdit(serverPlayer, taskScreen)) {
                taskScreen.loadAdditional(message.payload, serverPlayer.registryAccess());
                serverPlayer.level().sendBlockUpdated(taskScreen.getBlockPos(), taskScreen.getBlockState(), taskScreen.getBlockState(), Block.UPDATE_ALL);
                taskScreen.setChanged();
            }
        });
    }
}
