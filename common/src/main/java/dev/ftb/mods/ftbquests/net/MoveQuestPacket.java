package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MoveQuestPacket extends BaseC2SPacket {
	private final long id;
	private final long chapter;
	private final double x, y;

	MoveQuestPacket(FriendlyByteBuf buffer) {
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MoveQuestPacket(long i, long c, double _x, double _y) {
		id = i;
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public PacketID getId() {
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
			new MoveQuestResponsePacket(id, chapter, x, y).sendToAll(context.getPlayer().getServer());
		}
	}
}