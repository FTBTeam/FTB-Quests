package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

public class ClaimAllRewardsMessage implements CustomPacketPayload {
	public static final Type<ClaimAllRewardsMessage> TYPE = new Type<>(FTBQuestsAPI.id("claim_all_rewards_message"));

	public static final ClaimAllRewardsMessage INSTANCE = new ClaimAllRewardsMessage();

	public static final StreamCodec<FriendlyByteBuf, ClaimAllRewardsMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ClaimAllRewardsMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimAllRewardsMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			ServerQuestFile.getInstance().getTeamData(player).ifPresent(data -> {
				data.getFile().forAllQuests(quest -> {
					if (data.isCompleted(quest)) {
						quest.getRewards().stream()
								.filter(reward -> !reward.getExcludeFromClaimAll())
								.forEach(reward -> data.claimReward(player, reward, true));
					}
				});
			});
		});
	}
}
