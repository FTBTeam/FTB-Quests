package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class SyncEditingModeMessage extends BaseS2CMessage {
	private final UUID uuid;
	private final boolean editingMode;

	public SyncEditingModeMessage(FriendlyByteBuf buffer) {
		uuid = buffer.readUUID();
		editingMode = buffer.readBoolean();
	}

	public SyncEditingModeMessage(UUID id, boolean e) {
		uuid = id;
		editingMode = e;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SYNC_EDITING_MODE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(uuid);
		buffer.writeBoolean(editingMode);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.syncEditingMode(uuid, editingMode);
	}
}