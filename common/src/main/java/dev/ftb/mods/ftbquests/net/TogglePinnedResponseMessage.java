package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class TogglePinnedResponseMessage extends BaseS2CMessage {
	private final long id;
	private final boolean pinned;

	TogglePinnedResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		pinned = buffer.readBoolean();
	}

	public TogglePinnedResponseMessage(long i, boolean p) {
		id = i;
		pinned = p;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.TOGGLE_PINNED_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(pinned);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.togglePinned(id, pinned);
	}
}