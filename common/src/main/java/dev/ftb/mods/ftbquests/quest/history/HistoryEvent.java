package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.nbt.CompoundTag;

public abstract class HistoryEvent {
    public void undo(ServerQuestFile file) {
        applyUndo(file);
        file.clearCachedData();
        file.markDirty();
    }

    protected abstract void applyUndo(ServerQuestFile file);

    public static class Creation extends HistoryEvent {
        private final long id;

        public Creation(QuestObjectBase obj) {
            this.id = obj.id;
        }

        @Override
        protected void applyUndo(ServerQuestFile file) {
            file.deleteObject(id);
        }
    }

    public static class Deletion extends HistoryEvent {
        private final long id;
        private final long parentId;
        private final QuestObjectType type;
        private final CompoundTag tag;
        private final CompoundTag extra;

        public Deletion(QuestObjectBase obj) {
            this.id = obj.getId();
            this.parentId = obj.getParentID();
            this.tag = new CompoundTag();
            this.type = obj.getObjectType();
            this.extra = obj.makeExtraCreationData();
            ServerQuestFile.INSTANCE.writeData(tag, obj.holderLookup());
        }

        @Override
        protected void applyUndo(ServerQuestFile file) {
            QuestObjectBase object = file.create(id, type, parentId, extra);

            object.readData(tag, file.holderLookup());
            object.onCreated();
            object.getQuestFile().refreshIDMap();
            object.getQuestFile().markDirty();
            object.getQuestFile().getTranslationManager().processInitialTranslation(extra, object);

            NetworkHelper.sendToAll(file.server, CreateObjectResponseMessage.create(object, extra,null));
        }
    }
}
