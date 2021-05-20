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
public class ResetRewardPacket extends BaseS2CPacket {
	private final UUID team;
	private final UUID player;
	private final long id;

	ResetRewardPacket(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		player = buffer.readUUID();
		id = buffer.readLong();
	}

	public ResetRewardPacket(UUID t, UUID p, long i) {
		team = t;
		player = p;
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.RESET_REWARD;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeUUID(player);
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.resetReward(team, player, id);
	}
}