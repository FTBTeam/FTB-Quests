package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
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
		FTBQuests.NET_PROXY.togglePinned(id, pinned);
	}
}