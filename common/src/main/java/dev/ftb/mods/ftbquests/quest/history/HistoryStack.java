package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryStack {
    // TODO limit size of undo/redo stacks (expel oldest members if size > threshold)
    private final Deque<HistoryEvent> undoStack = new ArrayDeque<>();
    private final Deque<HistoryEvent> redoStack = new ArrayDeque<>();

    public void addAndApply(ServerQuestFile file, HistoryEvent event) {
        undoStack.push(event);

        event.apply(file);
    }

    public void add(HistoryEvent event) {
        undoStack.push(event);
    }
}
