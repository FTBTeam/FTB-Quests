package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClaimRewardResponseMessage extends BaseS2CMessage {
	private final UUID team;
	private final UUID player;
	private final long reward;

	ClaimRewardResponseMessage(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		player = buffer.readUUID();
		reward = buffer.readLong();
	}

	public ClaimRewardResponseMessage(UUID t, UUID p, long r) {
		team = t;
		player = p;
		reward = r;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.CLAIM_REWARD_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeUUID(player);
		buffer.writeLong(reward);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.claimReward(team, player, reward);
	}
}