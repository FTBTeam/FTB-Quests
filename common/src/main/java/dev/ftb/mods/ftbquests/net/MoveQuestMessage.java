package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveQuestMessage extends BaseC2SMessage {
	private final long id;
	private final long chapter;
	private final double x, y;

	MoveQuestMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveQuestMessage(long i, long c, double _x, double _y) {
		id = i;
		chapter = c;
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
		buffer.writeLong(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		Quest quest = ServerQuestFile.INSTANCE.getQuest(id);

		if (quest != null) {
			quest.moved(x, y, chapter);
			ServerQuestFile.INSTANCE.save();
			new MoveQuestResponseMessage(id, chapter, x, y).sendToAll(context.getPlayer().getServer());
		}
	}
}