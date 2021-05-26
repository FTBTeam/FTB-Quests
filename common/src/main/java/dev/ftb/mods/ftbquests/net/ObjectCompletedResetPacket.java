package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ObjectCompletedResetPacket extends BaseS2CPacket {
	private final UUID team;
	private final long id;

	public ObjectCompletedResetPacket(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		id = buffer.readLong();
	}

	public ObjectCompletedResetPacket(UUID t, long i) {
		team = t;
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.OBJECT_COMPLETED_RESET;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.objectCompleted(team, id, null);
	}
}