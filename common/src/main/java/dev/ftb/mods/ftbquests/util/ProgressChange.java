package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Date;
import java.util.UUID;

public class ProgressChange {
	private final QuestFile file;
	private final Date date;
	private final QuestObjectBase origin;
	private final UUID playerId;
	private boolean reset;
	private boolean notifications;

	public ProgressChange(QuestFile file, QuestObjectBase origin, UUID playerId) {
		this.file = file;
		this.origin = origin;
		this.playerId = playerId;
		this.date = new Date();

		reset = true;
		notifications = false;
	}

	public ProgressChange(QuestFile f, FriendlyByteBuf buffer) {
		file = f;
		date = new Date();
		origin = file.getBase(buffer.readLong());
		reset = buffer.readBoolean();
		playerId = buffer.readUUID();
		notifications = buffer.readBoolean();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(origin == null ? 0L : origin.id);
		buffer.writeBoolean(reset);
		buffer.writeUUID(playerId);
		buffer.writeBoolean(notifications);
	}

	public void maybeForceProgress(UUID teamId) {
		if (origin != null) {
			TeamData t = ServerQuestFile.INSTANCE.getData(teamId);
			origin.forceProgressRaw(t, this);
		}
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public Date getDate() {
		return date;
	}

	public ProgressChange setReset(boolean reset) {
		this.reset = reset;
		return this;
	}

	public boolean shouldReset() {
		return reset;
	}

	public ProgressChange withNotifications() {
		notifications = true;
		return this;
	}

	public boolean shouldNotify() {
		return notifications;
	}
}
