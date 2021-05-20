package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveChapterGroupResponsePacket extends BaseS2CPacket {
	private final long id;
	private final boolean up;

	MoveChapterGroupResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MoveChapterGroupResponsePacket(long i, boolean u) {
		id = i;
		up = u;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.MOVE_CHAPTER_GROUP_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeBoolean(up);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.moveChapterGroup(id, up);
	}
}