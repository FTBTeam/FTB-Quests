package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.ChangeChapterGroupResponseMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public record MoveChapterAcrossGroup(long chapterId, long oldGroupId, long newGroupId) implements QuestBookEditEvent {
    @Override
    public void apply(ServerQuestFile file) {
        applyChange(file, newGroupId);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        applyChange(file, oldGroupId);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of(Component.translatable("ftbquests.event.move_chapter_across",
                QuestObjectType.CHAPTER.getDisplayString(),
                getTitle(file, chapterId),
                QuestObjectType.CHAPTER_GROUP.getDisplayString(),
                getTitle(file, newGroupId))
        );
    }

    private void applyChange(ServerQuestFile file, long groupId) {
        if (file.getChapter(chapterId) instanceof Chapter chapter && file.getChapterGroup(groupId) instanceof ChapterGroup group) {
            if (group != chapter.getGroup()) {
                chapter.getGroup().removeChapter(chapter);
                group.addChapter(chapter);

                Server2PlayNetworking.sendToAllPlayers(file.server, new ChangeChapterGroupResponseMessage(chapterId, groupId));
            }
        }
    }
}
