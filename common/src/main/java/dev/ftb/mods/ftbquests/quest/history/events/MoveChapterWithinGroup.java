package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.MoveChapterResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public record MoveChapterWithinGroup(long chapterId, boolean movingUp) implements QuestBookEditEvent {
    @Override
    public void apply(ServerQuestFile file) {
        applyChange(file, movingUp);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        applyChange(file, !movingUp);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of(Component.translatable("ftbquests.event.move_object",
                QuestObjectType.CHAPTER.getDisplayString(),
                getTitle(file, chapterId),
                movingUp ? "▲" : "▼")
        );
    }

    private void applyChange(ServerQuestFile file, boolean dir) {
        if (file.getChapter(chapterId) instanceof Chapter chapter && chapter.getGroup().moveChapterWithinGroup(chapter, dir)) {
            Server2PlayNetworking.sendToAllPlayers(file.server, new MoveChapterResponseMessage(chapterId, dir));
        }
    }
}
