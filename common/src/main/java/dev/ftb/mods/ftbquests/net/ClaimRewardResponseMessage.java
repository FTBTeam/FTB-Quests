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

public record ClaimRewardResponseMessage(UUID team, UUID player, long reward) implements CustomPacketPayload {
	public static final Type<ClaimRewardResponseMessage> TYPE = new Type<>(FTBQuestsAPI.id("claim_reward_response_message"));

	public static final StreamCodec<FriendlyByteBuf, ClaimRewardResponseMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ClaimRewardResponseMessage::team,
			UUIDUtil.STREAM_CODEC, ClaimRewardResponseMessage::player,
			ByteBufCodecs.VAR_LONG, ClaimRewardResponseMessage::reward,
			ClaimRewardResponseMessage::new
	);

	@Override
	public Type<ClaimRewardResponseMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimRewardResponseMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.claimReward(message.team, message.player, message.reward));
	}
}
