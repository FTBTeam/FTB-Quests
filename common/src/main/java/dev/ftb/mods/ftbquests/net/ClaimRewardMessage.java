package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ClaimRewardMessage extends BaseC2SMessage {
	private final long id;
	private final boolean notify;

	ClaimRewardMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		notify = buffer.readBoolean();
	}

	public ClaimRewardMessage(long i, boolean n) {
		id = i;
		notify = n;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CLAIM_REWARD;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(notify);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		Reward reward = ServerQuestFile.INSTANCE.getReward(id);
		ServerPlayer player = (ServerPlayer) context.getPlayer();

		if (reward != null) {
			TeamData teamData = ServerQuestFile.INSTANCE.getData(player);

			if (teamData.isCompleted(reward.quest)) {
				teamData.claimReward(player, reward, notify);
			}
		}
	}
}