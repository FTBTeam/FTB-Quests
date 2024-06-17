package dev.ftb.mods.ftbquests.events;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.net.DisplayCompletionToastMessage;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class QuestProgressEventData<T extends QuestObject> {
	private final Date time;
	private final TeamData teamData;
	private final T object;
	private final List<ServerPlayer> onlineMembers;
	private final List<ServerPlayer> notifiedPlayers;

	public QuestProgressEventData(Date date, TeamData teamData, T object, Collection<ServerPlayer> online, Collection<ServerPlayer> notified) {
		time = date;
		this.teamData = teamData;
		this.object = object;
		onlineMembers = new ArrayList<>(online);
		notifiedPlayers = new ArrayList<>(notified);
	}

	public void setStarted(long id) {
		teamData.setStarted(id, time);
	}

	public void setCompleted(long id) {
		teamData.setCompleted(id, time);
	}

	public void notifyPlayers(long id) {
		notifiedPlayers.forEach(player -> NetworkManager.sendToPlayer(player, new DisplayCompletionToastMessage(id)));
	}

	public Date getTime() {
		return time;
	}

	public TeamData getTeamData() {
		return teamData;
	}

	public T getObject() {
		return object;
	}

	public List<ServerPlayer> getOnlineMembers() {
		return onlineMembers;
	}

	public List<ServerPlayer> getNotifiedPlayers() {
		return notifiedPlayers;
	}

	public <N extends QuestObject> QuestProgressEventData<N> withObject(N o) {
		return object == o ? (QuestProgressEventData<N>) this : new QuestProgressEventData<>(time, teamData, o, onlineMembers, notifiedPlayers);
	}
}
