package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ClaimChoiceRewardPacket extends BaseC2SPacket {
	private final long id;
	private final int index;

	public ClaimChoiceRewardPacket(long i, int idx) {
		id = i;
		index = idx;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CLAIM_CHOICE_REWARD;
	}

	ClaimChoiceRewardPacket(FriendlyByteBuf buffer) {
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

			if (r.getTable() != null && data.isCompleted(reward.quest)) {
				if (index >= 0 && index < r.getTable().rewards.size()) {
					r.getTable().rewards.get(index).reward.claim(player, true);
					data.claimReward(player, reward, true);
				}
			}
		}
	}
}