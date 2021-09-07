package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseC2SMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
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

		if (reward instanceof ChoiceReward) {
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			ChoiceReward r = (ChoiceReward) reward;
			TeamData data = TeamData.get(player);
			RewardTable table = r.getTable();

			if (table != null && data.isCompleted(reward.quest)) {
				if (index >= 0 && index < table.rewards.size()) {
					table.rewards.get(index).reward.claim(player, true);
					data.claimReward(player, reward, true);
				}
			}
		}
	}
}