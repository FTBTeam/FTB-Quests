package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A history event records a logical operation done to the quest file which creates, deletes or modifies one or more
 * quest objects, as part of a single operation. E.g. adding a quest and task together, or modifying multiple quest
 * objects together. Each history event is a single operation for the purposes of undo/redo functionality.
 */
public interface HistoryEvent {
    /**
     * Apply this history event to the quest file. Called both to carry out an edit to the file, and later on if
     * an undo of the edit needs to be redone (undo an undo...)
     *
     * @param file the quest file
     */
    void apply(ServerQuestFile file);

    /**
     * Reverse the application of this history event. Should be processed in the opposite order of
     * {@link #apply(ServerQuestFile)} operation in the case that there are multiple quest objects involved (e.g.
     * when adding a task via context menu, the history event contains both a quest and a task in that order; undoing
     * this should remove the task first, then the quest).
     *
     * @param file the quest file
     */
    void applyUndo(ServerQuestFile file);

    record Creation(List<CreateDeleteRecord> creationRecords) implements HistoryEvent {
        @Override
        public void apply(ServerQuestFile file) {
            create(file, creationRecords);
        }

        @Override
        public void applyUndo(ServerQuestFile file) {
            delete(file, creationRecords.reversed());
        }
    }

    record Deletion(List<CreateDeleteRecord> deletionRecords) implements HistoryEvent {
        @Override
        public void apply(ServerQuestFile file) {
            delete(file, deletionRecords);
        }

        @Override
        public void applyUndo(ServerQuestFile file) {
            create(file, deletionRecords.reversed());
        }
    }

    record Modification(List<EditRecord> oldRecords, List<EditRecord> newRecords) implements HistoryEvent {
        public Modification {
            Validate.isTrue(oldRecords.size() == newRecords.size(), "old and new records lists must be the same size!");
            for (int i = 0; i < oldRecords.size(); i++) {
                Validate.isTrue(oldRecords.get(i).id() == newRecords.get(i).id(), "record " + i + "has mismatched ids!");
            }
        }

        public static Optional<Modification> fromEditRecords(ServerQuestFile file, List<EditRecord> newRecs) {
            List<EditRecord> oldRecs = newRecs.stream()
                    .map(EditRecord::id)
                    .map(file::get)
                    .filter(Objects::nonNull)
                    .map(EditRecord::ofQuestObject)
                    .toList();
            return oldRecs.size() == newRecs.size() ?
                    Optional.of(new Modification(oldRecs, newRecs)) :
                    Optional.empty();
        }

        @Override
        public void apply(ServerQuestFile file) {
            edit(file, newRecords);
        }

        @Override
        public void applyUndo(ServerQuestFile file) {
            edit(file, oldRecords);
        }
    }

    private static void create(ServerQuestFile file, List<CreateDeleteRecord> records) {
        for (var creationRec : records) {
            QuestObjectBase qo = file.create(creationRec.id(), creationRec.questObjectType(), creationRec.parent(), creationRec.extra());
            qo.readData(creationRec.nbt(), file.server.registryAccess());
            file.getTranslationManager().processInitialTranslation(creationRec.extra(), qo);

            qo.onCreated();
        }
        file.markDirty();
    }

    private static void delete(ServerQuestFile file, List<CreateDeleteRecord> records) {
        List<Long> ids = records.stream().map(CreateDeleteRecord::id).toList();
        file.deleteObjects(ids);
    }

    private static void edit(ServerQuestFile file, List<EditRecord> records) {
        records.forEach(editRecord -> {
            QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(editRecord.id());
            if (object != null) {
                object.readData(editRecord.nbt(), file.server.registryAccess());
                object.editedFromGUIOnServer();
                file.clearCachedData();
            }
        });
        file.markDirty();
    }
}
