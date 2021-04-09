package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageSyncLock extends MessageBase {
	private final UUID id;
	private final boolean lock;

	public MessageSyncLock(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		lock = buffer.readBoolean();
	}

	public MessageSyncLock(UUID i, boolean e) {
		id = i;
		lock = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeBoolean(lock);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.syncLock(id, lock);
	}
}