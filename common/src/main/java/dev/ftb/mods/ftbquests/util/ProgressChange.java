package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Date;
import java.util.UUID;

public class ProgressChange {
	public final QuestFile file;
	public Date time;
	public QuestObjectBase origin;
	public boolean reset;
	public UUID player;
	public boolean notifications;

	public ProgressChange(QuestFile f) {
		file = f;
		time = new Date();
		origin = null;
		reset = true;
		player = Util.NIL_UUID;
		notifications = false;
	}

	public ProgressChange(QuestFile f, FriendlyByteBuf buffer) {
		file = f;
		time = new Date();
		origin = file.getBase(buffer.readLong());
		reset = buffer.readBoolean();
		player = buffer.readUUID();
		notifications = buffer.readBoolean();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(origin == null ? 0L : origin.id);
		buffer.writeBoolean(reset);
		buffer.writeUUID(player);
		buffer.writeBoolean(notifications);
	}
}
