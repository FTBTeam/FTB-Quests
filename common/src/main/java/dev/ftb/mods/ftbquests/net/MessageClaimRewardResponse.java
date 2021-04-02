package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageBase {
	private final UUID team;
	private final UUID player;
	private final long reward;

	MessageClaimRewardResponse(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		player = buffer.readUUID();
		reward = buffer.readLong();
	}

	public MessageClaimRewardResponse(UUID t, UUID p, long r) {
		team = t;
		player = p;
		reward = r;
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