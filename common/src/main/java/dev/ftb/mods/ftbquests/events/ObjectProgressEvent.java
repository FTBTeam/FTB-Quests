package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.List;

public abstract class ObjectProgressEvent<T extends QuestObject> {
    protected final QuestProgressEventData<T> data;

    protected ObjectProgressEvent(QuestProgressEventData<T> d) {
        data = d;
    }

    public boolean isCancelable() {
        return true;
    }

    public Date getTime() {
        return data.getTime();
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
