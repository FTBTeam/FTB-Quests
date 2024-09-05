package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryStack {
    private final Deque<HistoryEvent> undoStack = new ArrayDeque<>();
    private final Deque<HistoryEvent> redoStack = new ArrayDeque<>();

    public void addAndApply(ServerQuestFile file, HistoryEvent event) {
        undoStack.push(event);

        event.apply(file);
    }
}
