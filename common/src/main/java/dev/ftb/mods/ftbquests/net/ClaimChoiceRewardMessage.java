package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ClaimChoiceRewardMessage extends BaseC2SMessage {
	private final long id;
	private final int index;

	public ClaimChoiceRewardMessage(long i, int idx) {
		id = i;
		index = idx;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CLAIM_CHOICE_REWARD;
	}

	ClaimChoiceRewardMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		index = buffer.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeVarInt(index);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);

		if (reward instanceof ChoiceReward choiceReward && context.getPlayer() instanceof ServerPlayer serverPlayer) {
			TeamData data = TeamData.get(serverPlayer);
			RewardTable table = choiceReward.getTable();

			if (table != null && data.isCompleted(reward.quest)) {
				if (index >= 0 && index < table.rewards.size()) {
					table.rewards.get(index).reward.claim(serverPlayer, true);
					data.claimReward(serverPlayer, reward, true);
				}
			}
		}
	}
}
