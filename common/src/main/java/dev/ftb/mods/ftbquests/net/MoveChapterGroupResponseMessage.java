package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveChapterGroupResponseMessage extends BaseS2CMessage {
	private final long id;
	private final boolean up;

	MoveChapterGroupResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		up = buffer.readBoolean();
	}

	public MoveChapterGroupResponseMessage(long i, boolean u) {
		id = i;
		up = u;
	}

	@Override
	public MessageType getType() {
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