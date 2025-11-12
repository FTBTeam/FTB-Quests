package dev.ftb.mods.ftbquests.util;

import dev.ftb.mods.ftbquests.net.ClearRepeatCooldownMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Date;
import java.util.UUID;

public class ProgressChange {
	public static StreamCodec<FriendlyByteBuf, ProgressChange> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ProgressChange::getOriginId,
			ByteBufCodecs.BOOL, ProgressChange::shouldReset,
			UUIDUtil.STREAM_CODEC, ProgressChange::getPlayerId,
			ByteBufCodecs.BOOL, ProgressChange::shouldNotify,
			ProgressChange::createServerSide
	);

	private final Date date;
	private final QuestObjectBase origin;
	private final UUID playerId;
	private boolean reset;
	private boolean notifications;

	public ProgressChange(QuestObjectBase origin, UUID playerId) {
		this.origin = origin;
		this.playerId = playerId;
		this.date = new Date();

		reset = true;
		notifications = false;
	}

	public static ProgressChange createServerSide(long origin, boolean reset, UUID playerId, boolean notifications) {
		ProgressChange pc = new ProgressChange(ServerQuestFile.INSTANCE.getBase(origin), playerId);
		pc.reset = reset;
		pc.notifications = notifications;
		return pc;
	}

	public void maybeForceProgress(UUID teamId) {
		if (origin != null) {
			TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(teamId);
			origin.forceProgressRaw(data, this);
			if (origin instanceof Quest quest && reset) {
				data.clearRepeatCooldown(quest);
				ClearRepeatCooldownMessage.sendToTeam(quest, data.getTeamId());
			}
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

	private long getOriginId() {
		return origin == null ? 0L : origin.id;
	}
}
