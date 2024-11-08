package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ClaimRewardMessage(long id, boolean shouldNotify) implements CustomPacketPayload {
	public static final Type<ClaimRewardMessage> TYPE = new Type<>(FTBQuestsAPI.rl("claim_reward_message"));

	public static final StreamCodec<FriendlyByteBuf, ClaimRewardMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ClaimRewardMessage::id,
			ByteBufCodecs.BOOL, ClaimRewardMessage::shouldNotify,
			ClaimRewardMessage::new
	);

	@Override
	public Type<ClaimRewardMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimRewardMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			Reward reward = ServerQuestFile.INSTANCE.getReward(message.id);

			if (reward != null && context.getPlayer() instanceof ServerPlayer player) {
				ServerQuestFile.INSTANCE.getTeamData(player).ifPresent(teamData -> {
					if (teamData.isCompleted(reward.getQuest())) {
						teamData.claimReward(player, reward, message.shouldNotify);
					}
				});
			}
		});
	}
}