package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TaskScreenConfigRequestMessage(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TaskScreenConfigRequestMessage> TYPE = new Type<>(FTBQuestsAPI.rl("task_screen_config_request_message"));

    public static final StreamCodec<FriendlyByteBuf, TaskScreenConfigRequestMessage> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, TaskScreenConfigRequestMessage::pos,
	    TaskScreenConfigRequestMessage::new
    );

    @Override
    public Type<TaskScreenConfigRequestMessage> type() {
        return TYPE;
    }

    public static void handle(TaskScreenConfigRequestMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBQuestsClient.openScreenConfigGui(message.pos));
    }
}
