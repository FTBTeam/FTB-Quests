package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ClaimRewardMessage(long id, boolean shouldNotify) implements CustomPacketPayload {
	public static final Type<ClaimRewardMessage> TYPE = new Type<>(FTBQuestsAPI.id("claim_reward_message"));

	public static final StreamCodec<FriendlyByteBuf, ClaimRewardMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ClaimRewardMessage::id,
			ByteBufCodecs.BOOL, ClaimRewardMessage::shouldNotify,
			ClaimRewardMessage::new
	);

	@Override
	public Type<ClaimRewardMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimRewardMessage message, PacketContext context) {
		Reward reward = ServerQuestFile.getInstance().getReward(message.id);

		if (reward != null && context.player() instanceof ServerPlayer player) {
			ServerQuestFile.getInstance().getTeamData(player).ifPresent(teamData -> {
				if (teamData.isCompleted(reward.getQuest())) {
					teamData.claimReward(player, reward, message.shouldNotify);
				}
			});
		}
	}
}
