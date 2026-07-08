package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.MoveMovableResponseMessage;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record MoveMovableObject(
        long movableId, QuestObjectType questObjectType,
        Component title,
        long oldChapterId, long newChapterId,
        double oldX, double oldY,
        double newX, double newY
) implements QuestBookEditEvent
{
    public static Optional<MoveMovableObject> create(Movable movable, Chapter newChapter, double newX, double newY) {
        if (newX != movable.getX() || newY != movable.getY() || !Objects.equals(newChapter, movable.getChapter())) {
            return Optional.of(new MoveMovableObject(
                    movable.getMovableID(), movable.getObjectType(),
                    movable.getTitle(),
                    movable.getChapter().getId(), newChapter.getId(),
                    movable.getX(), movable.getY(), newX, newY
            ));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void apply(ServerQuestFile file) {
        applyChange(file, newX, newY, newChapterId);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        applyChange(file, oldX, oldY, oldChapterId);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        Chapter newChapter = file.getChapter(newChapterId);
        return oldChapterId == newChapterId ?
                List.of(Component.translatable("ftbquests.event.move_movable",
                        questObjectType.getDisplayString(),
                        title
                )) :
                List.of(Component.translatable("ftbquests.event.move_chapter_across",
                        questObjectType.getDisplayString(),
                        title,
                        QuestObjectType.CHAPTER.getDisplayString(),
                        newChapter != null ? newChapter.getTitle() : Component.literal("<?>")
                ));

    }

    private void applyChange(ServerQuestFile file, double x, double y, long chapterId) {
        if (file.getBase(movableId) instanceof Movable movable && file.getChapter(chapterId) instanceof Chapter chapter) {
            movable.setPosition(x, y);
            movable.setChapter(chapter);

            NetworkHelper.sendToAll(file.server, MoveMovableResponseMessage.create(movable));
        }
    }
}
