package dev.ftb.mods.ftbquests.quest.history;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.SendChangeDescPacket;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.NoAction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class HistoryStack {
    private static final int MAX_UNDO_SIZE = 100;

    private final Deque<QuestBookEditEvent> undoStack = new ArrayDeque<>();
    private final Deque<QuestBookEditEvent> redoStack = new ArrayDeque<>();

    public void addAndApply(ServerQuestFile file, QuestBookEditEvent event) {
        pushWithSizeLimit(event, undoStack);

        event.apply(file);

        NetworkHelper.sendToAll(file.server, createDescPacket(file, event, ChangeType.INITIAL));
    }

    public boolean tryUndo(ServerQuestFile file) {
        return applyOperation(undoStack, redoStack, event -> {
            event.applyUndo(file);
            NetworkHelper.sendToAll(file.server, createDescPacket(file, event, ChangeType.UNDO));
        });
    }

    public boolean tryRedo(ServerQuestFile file) {
        return applyOperation(redoStack, undoStack, event -> {
            event.apply(file);
            NetworkHelper.sendToAll(file.server, createDescPacket(file, event, ChangeType.REDO));
        });
    }

    public SendChangeDescPacket createInitialDescPacket(ServerQuestFile file) {
        // sent to players on login to get their gui undo/redo buttons set up correctly
        return createDescPacket(file, NoAction.INSTANCE, ChangeType.INITIAL);
    }

    private SendChangeDescPacket createDescPacket(ServerQuestFile file, QuestBookEditEvent event, ChangeType type) {
        return new SendChangeDescPacket(type, event.description(file, type),
                undoStack.isEmpty() ? List.of() : undoStack.peek().description(file, ChangeType.UNDO),
                redoStack.isEmpty() ? List.of() : redoStack.peek().description(file, ChangeType.REDO)
        );
    }

    private boolean applyOperation(Deque<QuestBookEditEvent> from, Deque<QuestBookEditEvent> to, Consumer<QuestBookEditEvent> consumer) {
        if (!from.isEmpty()) {
            var event = from.pop();

            pushWithSizeLimit(event, to);

            consumer.accept(event);

            return true;
        }
        return false;
    }

    private void pushWithSizeLimit(QuestBookEditEvent event, Deque<QuestBookEditEvent> stack) {
        stack.push(event);
        while (stack.size() > MAX_UNDO_SIZE) {
            stack.removeLast();
        }
    }
}
