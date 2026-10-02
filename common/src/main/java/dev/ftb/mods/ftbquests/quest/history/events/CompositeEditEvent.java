package dev.ftb.mods.ftbquests.quest.history.events;

import com.google.common.collect.Lists;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pulls together multiple edit events of different types, which need to be handled as a single undo/redo unit.
 * <p>
 * Note: while an apply/undo operation bails if any member returns false, we don't have proper transaction support,
 * so it is in theory possible for a partial composite event to be applied or undone, although this should not
 * happen under normal circumstances.
 *
 * @param events the list of events
 */
public record CompositeEditEvent(List<QuestBookEditEvent> events) implements QuestBookEditEvent {
    public static CompositeEditEvent of(QuestBookEditEvent... events) {
        return new CompositeEditEvent(List.of(events));
    }

    @Override
    public boolean apply(ServerQuestFile file) {
        return events.stream().allMatch(e -> e.apply(file));
    }

    @Override
    public boolean applyUndo(ServerQuestFile file) {
        return Lists.reverse(events).stream().allMatch(e -> e.applyUndo(file));
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return Util.make(new ArrayList<>(), l ->
                events.forEach(e -> l.addAll(e.description(file, changeType)))
        );
    }
}
