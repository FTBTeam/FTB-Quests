package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.MoveChapterGroupResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public record MoveChapterGroup(long groupId, boolean movingUp) implements QuestBookEditEvent {
    @Override
    public boolean apply(ServerQuestFile file) {
        return applyChange(file, movingUp);
    }

    @Override
    public boolean applyUndo(ServerQuestFile file) {
        return applyChange(file, !movingUp);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of(Component.translatable("ftbquests.event.move_object",
                QuestObjectType.CHAPTER_GROUP.getDisplayString(),
                getTitle(file, groupId),
                movingUp ? "▲" : "▼")
        );
    }

    private boolean applyChange(ServerQuestFile file, boolean dir) {
        if (file.getChapterGroup(groupId) != null && file.moveChapterGroup(groupId, dir)) {
            NetworkHelper.sendToAll(file.server, new MoveChapterGroupResponseMessage(groupId, dir));
            return true;
        }
        return false;
    }
}
