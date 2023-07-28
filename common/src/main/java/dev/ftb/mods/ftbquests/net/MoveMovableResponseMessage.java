package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.Movable;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveMovableResponseMessage extends BaseS2CMessage {
	private final long id;
	private final long chapter;
	private final double x, y;

	MoveMovableResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveMovableResponseMessage(Movable movable, long c, double _x, double _y) {
		id = movable.getMovableID();
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_QUEST_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.moveQuest(id, chapter, x, y);
	}
}