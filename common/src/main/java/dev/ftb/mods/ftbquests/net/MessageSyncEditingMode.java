package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageSyncEditingMode extends MessageBase {
	private final boolean editingMode;

	public MessageSyncEditingMode(FriendlyByteBuf buffer) {
		editingMode = buffer.readBoolean();
	}

	public MessageSyncEditingMode(boolean e) {
		editingMode = e;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(editingMode);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.syncEditingMode(editingMode);
	}
}