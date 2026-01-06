package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.block.entity.EditableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public record BlockConfigResponseMessage(BlockPos pos, CompoundTag payload) implements CustomPacketPayload {
    public static final Type<BlockConfigResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("block_config_response_message"));

    public static final StreamCodec<FriendlyByteBuf, BlockConfigResponseMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockConfigResponseMessage::pos,
            ByteBufCodecs.COMPOUND_TAG, BlockConfigResponseMessage::payload,
            BlockConfigResponseMessage::new
    );

    @Override
    public Type<BlockConfigResponseMessage> type() {
        return TYPE;
    }

    public static void handle(BlockConfigResponseMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer
                    && serverPlayer.level().getBlockEntity(message.pos) instanceof EditableBlockEntity editable
                    && editable.hasPermissionToEdit(serverPlayer)) {
                editable.readPayload(message.payload, serverPlayer.registryAccess());
                serverPlayer.level().sendBlockUpdated(editable.getBlockPos(), editable.getBlockState(), editable.getBlockState(), Block.UPDATE_ALL);
                editable.setChanged();
            }
        });
    }
}
