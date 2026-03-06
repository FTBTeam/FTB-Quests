package dev.ftb.mods.ftbquests.events;

import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.net.NotifyCompletionMessage;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.jspecify.annotations.Nullable;

public final class QuestProgressEventData<T extends QuestObject> {
	@Nullable
	private final Date date;
	private final TeamData teamData;
	private final T object;
	private final List<ServerPlayer> onlineMembers;
	private final List<ServerPlayer> notifiedPlayers;

	public QuestProgressEventData(@Nullable Date date, TeamData teamData, T object, Collection<ServerPlayer> online, Collection<ServerPlayer> notified) {
		this.date = date;
		this.teamData = teamData;
		this.object = object;
		onlineMembers = new ArrayList<>(online);
		notifiedPlayers = new ArrayList<>(notified);
	}

	public static <T extends QuestObject> QuestProgressEventData<T> forClient(@Nullable Date date, TeamData teamData, T object) {
		return new QuestProgressEventData<>(date, teamData, object, List.of(), List.of());
	}

	public void setStarted(long id) {
		teamData.setStarted(id, date);
	}

	public void setCompleted(long id) {
		teamData.setCompleted(id, date);
	}

	public void notifyPlayers(long id) {
		notifiedPlayers.forEach(player -> NetworkManager.sendToPlayer(player, new NotifyCompletionMessage(id)));
	}

	@Nullable
	public Date getDate() {
		return date;
	}

	@Deprecated(forRemoval = true)
	@Nullable
	public Date getTime() {
		return getDate();
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
		return object == o ? (QuestProgressEventData<N>) this : new QuestProgressEventData<>(date, teamData, o, onlineMembers, notifiedPlayers);
	}
}
