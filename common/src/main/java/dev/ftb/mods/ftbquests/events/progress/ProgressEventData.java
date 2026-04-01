package dev.ftb.mods.ftbquests.events.progress;

import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.net.NotifyCompletionMessage;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public record ProgressEventData<T extends QuestObject>(
		@Nullable Date date,
		TeamData teamData,
		T object,
		Collection<ServerPlayer> onlineMembers,
		Collection<ServerPlayer> notifiedPlayers
) {
	public static <T extends QuestObject> ProgressEventData<T> forClient(@Nullable Date date, TeamData teamData, T object) {
		return new ProgressEventData<>(date, teamData, object, List.of(), List.of());
	}

	public void setStarted(long id) {
		teamData.setStarted(id, date);
	}

	public void setCompleted(long id) {
		teamData.setCompleted(id, date);
	}

	public void notifyPlayers(long id) {
		notifiedPlayers.forEach(player -> Server2PlayNetworking.send(player, new NotifyCompletionMessage(id)));
	}

	public <N extends QuestObject> ProgressEventData<N> withObject(N o) {
		return object == o ? (ProgressEventData<N>) this : new ProgressEventData<>(date, teamData, o, onlineMembers, notifiedPlayers);
	}
}
