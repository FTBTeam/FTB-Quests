package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ClaimChoiceRewardMessage(long id, int index) implements CustomPacketPayload {
	public static final Type<ClaimChoiceRewardMessage> TYPE = new Type<>(FTBQuestsAPI.rl("claim_choice_reward_message"));

	public static final StreamCodec<FriendlyByteBuf, ClaimChoiceRewardMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ClaimChoiceRewardMessage::id,
			ByteBufCodecs.VAR_INT, ClaimChoiceRewardMessage::index,
			ClaimChoiceRewardMessage::new
	);

	@Override
	public Type<ClaimChoiceRewardMessage> type() {
		return TYPE;
	}

	public static void handle(ClaimChoiceRewardMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> {
			Reward reward = ServerQuestFile.INSTANCE.getReward(message.id);

			if (reward instanceof ChoiceReward choiceReward && context.getPlayer() instanceof ServerPlayer serverPlayer) {
				ServerQuestFile.INSTANCE.getTeamData(serverPlayer).ifPresent(data -> {
					RewardTable table = choiceReward.getTable();

					if (table != null && data.isCompleted(reward.getQuest())) {
						if (message.index >= 0 && message.index < table.getWeightedRewards().size()) {
							table.getWeightedRewards().get(message.index).getReward().claim(serverPlayer, true);
							data.claimReward(serverPlayer, reward, true);
						}
					}
				});
			}
		});
	}
}