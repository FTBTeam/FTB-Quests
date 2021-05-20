package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class ClaimRewardPacket extends BaseC2SPacket {
	private final long id;
	private final boolean notify;

	ClaimRewardPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		notify = buffer.readBoolean();
	}

	public ClaimRewardPacket(long i, boolean n) {
		id = i;
		notify = n;
	}

	@Override
	public PacketID getId() {
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