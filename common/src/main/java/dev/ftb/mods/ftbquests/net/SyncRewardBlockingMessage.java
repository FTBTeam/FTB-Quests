package dev.ftb.mods.ftbquests.net;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

import java.util.UUID;

public record SyncRewardBlockingMessage(UUID teamId, boolean rewardsBlocked) implements CustomPacketPayload {
    public static final Type<SyncRewardBlockingMessage> TYPE = new Type<>(FTBQuestsAPI.id("sync_reward_blocking_message"));

    public static final StreamCodec<FriendlyByteBuf, SyncRewardBlockingMessage> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncRewardBlockingMessage::teamId,
            ByteBufCodecs.BOOL, SyncRewardBlockingMessage::rewardsBlocked,
	    SyncRewardBlockingMessage::new
    );

    @Override
    public Type<SyncRewardBlockingMessage> type() {
        return TYPE;
    }

    public static void handle(SyncRewardBlockingMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBQuestsNetClient.syncRewardBlocking(message.teamId, message.rewardsBlocked));
    }
}
