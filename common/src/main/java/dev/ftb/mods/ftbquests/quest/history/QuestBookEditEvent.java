package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.EditObjectResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A quest book edit event records a logical operation done to the server quest file which creates, deletes or modifies
 * one or more quest objects, as part of a single operation. E.g. adding a quest and task together, or modifying multiple
 * quest objects together. Each event is a single operation for the purposes of undo/redo functionality.
 * <p>
 * Implementations are responsible for both making changes the server-side book, and synchronizing those changes to clients.
 * These are typically (but not necessarily) invoked from the handlers of network packets received from the client.
 */
public interface QuestBookEditEvent {
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
     * when adding a task via context menu, the history event creates a quest and a task in that order; undoing
     * this should remove the task first, then the quest).
     *
     * @param file the quest file
     */
    void applyUndo(ServerQuestFile file);

    /**
     * {@return a brief description of the changes, 1 per change, for message console purposes}
     */
    List<Component> description(BaseQuestFile file, ChangeType changeType);

    default void createObjects(ServerQuestFile file, List<CreateOrDeleteRecord> records, @Nullable UUID creatorId) {
        for (var creationRec : records) {
            QuestObjectBase qo = file.create(creationRec.id(), creationRec.questObjectType(), creationRec.parent(), creationRec.extra());
            qo.readData(creationRec.nbt(), file.server.registryAccess());
            file.getTranslationManager().processInitialTranslation(creationRec.extra(), qo);
            qo.onCreated();
        }
        NetworkHelper.sendToAll(file.server, new CreateObjectResponseMessage(records, Optional.ofNullable(creatorId)));
        file.markDirty();
    }

    default void deleteObjects(ServerQuestFile file, List<CreateOrDeleteRecord> records) {
        List<Long> ids = records.stream().map(CreateOrDeleteRecord::id).toList();
        file.deleteObjects(ids);

        NetworkHelper.sendToAll(file.server, new DeleteObjectResponseMessage(ids));
    }

    default void editObjects(ServerQuestFile file, List<EditRecord> records) {
        records.forEach(editRecord -> {
            QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(editRecord.id());
            if (object != null) {
                object.readData(editRecord.nbt(), file.server.registryAccess());
                object.editedFromGUIOnServer();
                object.clearCachedData();

                NetworkHelper.sendToAll(file.server, new EditObjectResponseMessage(records));
            }
        });
        file.markDirty();
    }

    default Component getTitle(BaseQuestFile file, long id) {
        return file.getBase(id) instanceof QuestObjectBase c ? c.getTitle() : Component.literal("<?>");
    }
}
