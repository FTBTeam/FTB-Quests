package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseS2CPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.FTBQuests;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveQuestResponsePacket extends BaseS2CPacket {
	private final long id;
	private final long chapter;
	private final double x, y;

	MoveQuestResponsePacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveQuestResponsePacket(long i, long c, double _x, double _y) {
		id = i;
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public PacketID getId() {
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