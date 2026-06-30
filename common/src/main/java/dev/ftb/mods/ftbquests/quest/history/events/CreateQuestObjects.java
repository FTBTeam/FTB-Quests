package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CreateQuestObjects(List<CreateOrDeleteRecord> creationRecords, @Nullable UUID creator) implements QuestBookEditEvent {
    public CreateQuestObjects(CreateOrDeleteRecord rec, @Nullable UUID creator) {
        this(List.of(rec), creator);
    }

    @Override
    public void apply(ServerQuestFile file) {
        createObjects(file, creationRecords, creator);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        deleteObjects(file, creationRecords.reversed());
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        var recs = changeType.isUndo() ? creationRecords.reversed() : creationRecords;
        return recs.stream().map(rec ->
                Component.translatable("ftbquests.event.create",
                        rec.questObjectType().getDisplayString(),
                        rec.title())
        ).collect(Collectors.toList());
    }
}
