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
public class SyncLockMessage extends BaseS2CMessage {
	private final UUID id;
	private final boolean lock;

	public SyncLockMessage(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		lock = buffer.readBoolean();
	}

	public SyncLockMessage(UUID i, boolean e) {
		id = i;
		lock = e;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SYNC_LOCK;
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