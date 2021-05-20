package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class DeleteObjectResponsePacket extends BaseS2CPacket {
	private final long id;

	DeleteObjectResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
	}

	public DeleteObjectResponsePacket(long i) {
		id = i;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.DELETE_OBJECT_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.deleteObject(id);
	}
}