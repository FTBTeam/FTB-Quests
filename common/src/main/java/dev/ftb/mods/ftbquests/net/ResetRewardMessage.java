package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ResetRewardMessage extends BaseS2CMessage {
	private final UUID team;
	private final UUID player;
	private final long id;

	ResetRewardMessage(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		player = buffer.readUUID();
		id = buffer.readLong();
	}

	public ResetRewardMessage(UUID t, UUID p, long i) {
		team = t;
		player = p;
		id = i;
	}

	@Override
	public MessageType getType() {
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
