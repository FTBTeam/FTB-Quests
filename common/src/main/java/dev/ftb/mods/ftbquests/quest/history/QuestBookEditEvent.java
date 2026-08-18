package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.DeleteObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.EditObjectResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
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
     * The maximum number of quest objects which can be handled in one create/edit/delete request
     */
    int MAX_LIST_SIZE = 32;

    /**
     * Apply this history event to the quest file. Called both to carry out an edit to the file, and later on if
     * an undo of the edit needs to be redone (undo an undo...)
     *
     * @param file the quest file
     * @return true if the event was successfully applied, false otherwise
     */
    boolean apply(ServerQuestFile file);

    /**
     * Reverse the application of this history event. Should be processed in the opposite order of
     * {@link #apply(ServerQuestFile)} operation in the case that there are multiple quest objects involved (e.g.
     * when adding a task via context menu, the history event creates a quest and a task in that order; undoing
     * this should remove the task first, then the quest).
     *
     * @param file the quest file
     * @return true if the undo was successfully applied, false otherwise
     */
    boolean applyUndo(ServerQuestFile file);

    /**
     * {@return a brief description of the changes, 1 per change, for message console purposes}
     */
    List<Component> description(BaseQuestFile file, ChangeType changeType);

    default boolean createObjects(ServerQuestFile file, List<CreateOrDeleteRecord> records, @Nullable UUID creatorId) {
        try {
            for (var creationRec : records) {
                QuestObjectBase qo = file.create(creationRec.id(), creationRec.questObjectType(), creationRec.parent(), creationRec.metadata());
                qo.readData(creationRec.data(), file.server.registryAccess());
                file.getTranslationManager().processInitialTranslation(creationRec.metadata(), qo);
                qo.onCreated();
            }
            NetUtils.sendToQuestBookEditors(file.server, new CreateObjectResponseMessage(records, Optional.ofNullable(creatorId)));
            file.markDirty();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    default boolean deleteObjects(ServerQuestFile file, List<CreateOrDeleteRecord> records) {
        List<Long> ids = records.stream().map(CreateOrDeleteRecord::id).toList();
        boolean deleted = file.deleteObjects(ids);
        if (deleted) {
            NetUtils.sendToQuestBookEditors(file.server, new DeleteObjectResponseMessage(ids));
        }

        return deleted;
    }

    default boolean editObjects(ServerQuestFile file, List<EditRecord> records) {
        try {
            // all ids must be valid, or no change is made
            for (var rec : records) {
                if (file.getBase(rec.id()) == null) {
                    return false;
                }
            }

            for (var editRecord : records) {
                QuestObjectBase object = Objects.requireNonNull(file.getBase(editRecord.id())); // already validated non-null
                object.readData(editRecord.data(), file.server.registryAccess());
                object.editedFromGUIOnServer();
                object.clearCachedData();
            }

            NetUtils.sendToQuestBookEditors(file.server, new EditObjectResponseMessage(records));
            file.markDirty();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    default Component getTitle(BaseQuestFile file, long id) {
        return file.getBase(id) instanceof QuestObjectBase c ? c.getTitle() : Component.literal("<?>");
    }

    static <T> List<T> takeLimitedElements(List<T> list) {
        return list.subList(0, Math.min(list.size(), MAX_LIST_SIZE));
    }

    static Component sanitizeComponent(Component in) {
        String s = in.getString();
        return Component.literal(s.substring(0, Math.min(80, s.length())));
    }
}
