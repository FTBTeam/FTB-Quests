package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Date;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageObjectStarted extends MessageBase {
	private final UUID team;
	private final long id;

	public MessageObjectStarted(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		id = buffer.readLong();
	}

	public MessageObjectStarted(UUID t, long i) {
		team = t;
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.objectStarted(team, id, new Date());
	}
}