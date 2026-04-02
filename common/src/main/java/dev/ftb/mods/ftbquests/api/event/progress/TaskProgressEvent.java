package dev.ftb.mods.ftbquests.api.event.progress;

import dev.ftb.mods.ftbquests.quest.task.Task;

import java.util.function.Consumer;

public interface TaskProgressEvent extends Consumer<TaskProgressEvent.Data> {
    record Data(ProgressType type, ProgressEventData<Task> progressData) {
    }
}
