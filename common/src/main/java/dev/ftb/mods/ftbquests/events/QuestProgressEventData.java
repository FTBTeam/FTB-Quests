package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.List;

public final class QuestProgressEventData<T extends QuestObject> {
	public final Date time;
	public final TeamData teamData;
	public final T object;
	public final List<ServerPlayer> onlineMembers;
	public final List<ServerPlayer> notifiedPlayers;

	public QuestProgressEventData(Date i, TeamData t, T o, List<ServerPlayer> om, List<ServerPlayer> n) {
		time = i;
		teamData = t;
		object = o;
		onlineMembers = om;
		notifiedPlayers = n;
	}

	public <N extends QuestObject> QuestProgressEventData<N> withObject(N o) {
		return object == o ? (QuestProgressEventData<N>) this : new QuestProgressEventData<>(time, teamData, o, onlineMembers, notifiedPlayers);
	}
}
