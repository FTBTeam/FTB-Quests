package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class SyncEditingModePacket extends BaseS2CPacket {
	private final boolean editingMode;

	public SyncEditingModePacket(FriendlyByteBuf buffer) {
		editingMode = buffer.readBoolean();
	}

	public SyncEditingModePacket(boolean e) {
		editingMode = e;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.SYNC_EDITING_MODE;
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