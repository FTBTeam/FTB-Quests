package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ClaimRewardResponsePacket extends BaseS2CPacket {
	private final UUID team;
	private final UUID player;
	private final long reward;

	ClaimRewardResponsePacket(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		player = buffer.readUUID();
		reward = buffer.readLong();
	}

	public ClaimRewardResponsePacket(UUID t, UUID p, long r) {
		team = t;
		player = p;
		reward = r;
	}

	@Override
	public PacketID getId() {
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