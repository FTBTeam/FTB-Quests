package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageBase {
	private final UUID player;
	private final long id;

	MessageClaimRewardResponse(FriendlyByteBuf buffer) {
		player = NetUtils.readUUID(buffer);
		id = buffer.readLong();
	}

	public MessageClaimRewardResponse(UUID p, long i, int t) {
		player = p;
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		NetUtils.writeUUID(buffer, player);
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.claimReward(player, id);
	}
}