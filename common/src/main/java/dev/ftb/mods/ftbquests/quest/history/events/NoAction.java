package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public enum NoAction implements QuestBookEditEvent {
    INSTANCE;

    @Override
    public void apply(ServerQuestFile file) {
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of();
    }
}
