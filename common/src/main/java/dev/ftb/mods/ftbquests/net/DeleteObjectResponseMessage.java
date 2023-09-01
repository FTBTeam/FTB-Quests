package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class DeleteObjectResponseMessage extends BaseS2CMessage {
	private final long id;

	DeleteObjectResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public DeleteObjectResponseMessage(long i) {
		id = i;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.DELETE_OBJECT_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.deleteObject(id);
	}
}