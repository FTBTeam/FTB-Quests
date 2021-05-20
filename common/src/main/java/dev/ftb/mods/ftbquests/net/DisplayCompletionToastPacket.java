package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class DisplayCompletionToastPacket extends BaseS2CPacket {
	private final long id;

	DisplayCompletionToastPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public DisplayCompletionToastPacket(long i) {
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.DISPLAY_COMPLETION_TOAST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.displayCompletionToast(id);
	}
}