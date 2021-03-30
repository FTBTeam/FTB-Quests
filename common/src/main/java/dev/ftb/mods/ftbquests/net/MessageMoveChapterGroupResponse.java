package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageMoveChapterGroupResponse extends MessageBase {
	private final long id;
	private final boolean up;

	MessageMoveChapterGroupResponse(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MessageMoveChapterGroupResponse(long i, boolean u) {
		id = i;
		up = u;
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