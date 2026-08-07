package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.EditRecord;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public record ModifyQuestObjects(List<EditRecord> oldRecords, List<EditRecord> newRecords) implements QuestBookEditEvent {
    public ModifyQuestObjects {
        Validate.isTrue(oldRecords.size() == newRecords.size(), "old and new records lists must be the same size!");
        for (int i = 0; i < oldRecords.size(); i++) {
            Validate.isTrue(oldRecords.get(i).id() == newRecords.get(i).id(), "record " + i + " has mismatched ids!");
        }
    }

    public static Optional<ModifyQuestObjects> fromEditRecords(ServerQuestFile file, List<EditRecord> newRecs) {
        List<EditRecord> oldRecs = newRecs.stream()
                .map(EditRecord::id)
                .map(file::getBase)
                .filter(Objects::nonNull)
                .map(EditRecord::ofQuestObject)
                .toList();
        return oldRecs.size() == newRecs.size() && !oldRecs.equals(newRecs) ?
                Optional.of(new ModifyQuestObjects(oldRecs, newRecs)) :
                Optional.empty();
    }

    @Override
    public boolean apply(ServerQuestFile file) {
        return editObjects(file, newRecords);
    }

    @Override
    public boolean applyUndo(ServerQuestFile file) {
        return editObjects(file, oldRecords);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        var recs = changeType.isUndo() ? oldRecords.reversed() : oldRecords;
        return recs.stream().map(rec -> Component.translatable("ftbquests.event.modify",
                rec.questObjectType().getDisplayString(),
                rec.title())
        ).collect(Collectors.toList());
    }
}
