package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class ChangeChapterGroupResponsePacket extends BaseS2CPacket {
	private final long id;
	private final long group;

	public ChangeChapterGroupResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		group = buffer.readLong();
	}

	public ChangeChapterGroupResponsePacket(long i, long g) {
		id = i;
		group = g;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.CHANGE_CHAPTER_GROUP_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(group);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.changeChapterGroup(id, group);
	}
}