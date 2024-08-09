package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftbquests.quest.QuestObject;

import java.util.ArrayDeque;
import java.util.Deque;

public enum HistoryStack {
    INSTANCE;

    private final Deque<HistoryEvent> stack = new ArrayDeque<>();

    public void pushCreation(QuestObject object) {

    }
}
