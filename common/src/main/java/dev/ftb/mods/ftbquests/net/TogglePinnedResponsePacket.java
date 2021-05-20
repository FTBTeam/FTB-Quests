package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class TogglePinnedResponsePacket extends BaseS2CPacket {
	private final long id;

	TogglePinnedResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public TogglePinnedResponsePacket(long i) {
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.TOGGLE_PINNED_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.togglePinned(id);
	}
}