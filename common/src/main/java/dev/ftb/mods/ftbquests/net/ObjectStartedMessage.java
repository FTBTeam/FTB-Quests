package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Date;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ObjectStartedMessage extends BaseS2CMessage {
	private final UUID team;
	private final long id;

	public ObjectStartedMessage(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		id = buffer.readLong();
	}

	public ObjectStartedMessage(UUID t, long i) {
		team = t;
		id = i;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.OBJECT_STARTED;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.objectStarted(team, id, new Date());
	}
}