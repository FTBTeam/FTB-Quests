package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;

public class MoveMovableMessage extends BaseC2SMessage {
	private final long id;
	private final long chapterID;
	private final double x, y;

	MoveMovableMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapterID = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveMovableMessage(Movable obj, long c, double _x, double _y) {
		id = obj.getMovableID();
		chapterID = c;
		x = _x;
		y = _y;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.MOVE_QUEST;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(id);
		buffer.writeLong(chapterID);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		if (ServerQuestFile.INSTANCE.get(id) instanceof Movable movable) {
			movable.onMoved(x, y, chapterID);
			ServerQuestFile.INSTANCE.save();
			new MoveMovableResponseMessage(movable, chapterID, x, y).sendToAll(context.getPlayer().getServer());
		}
	}
}
