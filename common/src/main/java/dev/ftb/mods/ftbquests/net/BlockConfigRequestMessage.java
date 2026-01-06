package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BlockConfigRequestMessage(BlockPos pos, BlockType blockType) implements CustomPacketPayload {
    public static final Type<BlockConfigRequestMessage> TYPE = new Type<>(FTBQuestsAPI.id("block_config_request_message"));

    public static final StreamCodec<FriendlyByteBuf, BlockConfigRequestMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BlockConfigRequestMessage::pos,
            NetworkHelper.enumStreamCodec(BlockType.class), BlockConfigRequestMessage::blockType,
            BlockConfigRequestMessage::new
    );

    @Override
    public Type<BlockConfigRequestMessage> type() {
        return TYPE;
    }

    public static void handle(BlockConfigRequestMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            switch (message.blockType) {
                case TASK_SCREEN -> FTBQuestsClient.openTaskScreenConfigGui(message.pos);
                case BARRIER -> FTBQuestsClient.openBarrierConfigGui(message.pos);
            }
        });
    }

    public enum BlockType {
        TASK_SCREEN,
        BARRIER
    }
}
