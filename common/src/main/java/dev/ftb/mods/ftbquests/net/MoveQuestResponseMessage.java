package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveQuestResponseMessage extends BaseS2CMessage {
	private final long id;
	private final long chapter;
	private final double x, y;

	MoveQuestResponseMessage(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveQuestResponseMessage(long i, long c, double _x, double _y) {
		id = i;
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
		FTBQuests.NET_PROXY.moveQuest(id, chapter, x, y);
	}
}