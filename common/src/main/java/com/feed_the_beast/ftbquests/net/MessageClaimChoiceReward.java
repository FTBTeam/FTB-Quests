package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.ChoiceReward;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class MessageClaimChoiceReward extends MessageBase {
	private final long id;
	private final int index;

	public MessageClaimChoiceReward(long i, int idx) {
		id = i;
		index = idx;
	}

	MessageClaimChoiceReward(FriendlyByteBuf buffer) {
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
			PlayerData data = PlayerData.get(player);

			if (r.getTable() != null && data.isComplete(reward.quest)) {
				if (index >= 0 && index < r.getTable().rewards.size()) {
					r.getTable().rewards.get(index).reward.claim(player, true);
					data.claimReward(player, reward, true);
				}
			}
		}
	}
}