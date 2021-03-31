package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.time.Instant;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageObjectCompleted extends MessageBase {
	private final UUID team;
	private final long id;

	public MessageObjectCompleted(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		id = buffer.readLong();
	}

	public MessageObjectCompleted(UUID t, long i) {
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
		FTBQuests.NET_PROXY.objectCompleted(team, id, Instant.now());
	}
}