package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ClaimAllRewardsMessage extends BaseC2SMessage {
	public ClaimAllRewardsMessage() {
	}

	ClaimAllRewardsMessage(FriendlyByteBuf buffer) {
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CLAIM_ALL_REWARDS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		TeamData data = TeamData.get(player);

		ServerQuestFile.INSTANCE.forAllQuests(quest -> {
			if (data.isCompleted(quest)) {
				quest.getRewards().stream()
						.filter(reward -> !reward.getExcludeFromClaimAll())
						.forEach(reward -> data.claimReward(player, reward, true));
			}
		});
	}
}