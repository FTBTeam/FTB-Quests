package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public record DeleteQuestObjects(List<CreateOrDeleteRecord> deletionRecords) implements QuestBookEditEvent {
    public DeleteQuestObjects(CreateOrDeleteRecord rec) {
        this(List.of(rec));
    }

    @Override
    public void apply(ServerQuestFile file) {
        deleteObjects(file, deletionRecords);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        createObjects(file, deletionRecords.reversed(), null);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        var recs = changeType.isUndo() ? deletionRecords.reversed() : deletionRecords;
        return recs.stream().map(rec ->
                Component.translatable("ftbquests.event.delete",
                        rec.questObjectType().getDisplayString(),
                        rec.title())
        ).collect(Collectors.toList());
    }
}
