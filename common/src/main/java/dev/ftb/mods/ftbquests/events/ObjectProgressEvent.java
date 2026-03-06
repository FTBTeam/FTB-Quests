package dev.ftb.mods.ftbquests.events;

import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;

import java.util.Date;
import java.util.List;
import org.jspecify.annotations.Nullable;

public abstract class ObjectProgressEvent<T extends QuestObject> {
    protected final QuestProgressEventData<T> data;

    protected ObjectProgressEvent(QuestProgressEventData<T> d) {
        data = d;
    }

    public boolean isCancelable() {
        return true;
    }

    @Nullable
    public Date getDate() {
        return data.getDate();
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public Date getTime() {
        return getDate();
    }

    public TeamData getData() {
        return data.getTeamData();
    }

    public T getObject() {
        return data.getObject();
    }

    public List<ServerPlayer> getOnlineMembers() {
        return data.getOnlineMembers();
    }

    public List<ServerPlayer> getNotifiedPlayers() {
        return data.getNotifiedPlayers();
    }
}
