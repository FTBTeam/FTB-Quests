package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageChangeChapterGroupResponse extends MessageBase {
	private final long id;
	private final long group;

	public MessageChangeChapterGroupResponse(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		group = buffer.readLong();
	}

	public MessageChangeChapterGroupResponse(long i, long g) {
		id = i;
		group = g;
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