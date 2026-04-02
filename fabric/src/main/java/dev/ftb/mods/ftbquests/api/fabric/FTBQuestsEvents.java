package dev.ftb.mods.ftbquests.api.fabric;

import dev.ftb.mods.ftbquests.api.event.ClearFileCacheEvent;
import dev.ftb.mods.ftbquests.api.event.CustomRewardEvent;
import dev.ftb.mods.ftbquests.api.event.CustomTaskEvent;
import dev.ftb.mods.ftbquests.api.event.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.QuestProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.TaskProgressEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class FTBQuestsEvents {
    public static Event<ClearFileCacheEvent> CLEAR_FILE_CACHE
            = EventFactory.createArrayBacked(ClearFileCacheEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<CustomTaskEvent> CUSTOM_TASK
            = EventFactory.createArrayBacked(CustomTaskEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<CustomRewardEvent> CUSTOM_REWARD
            = EventFactory.createArrayBacked(CustomRewardEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<FileProgressEvent> FILE_PROGRESS
            = EventFactory.createArrayBacked(FileProgressEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<ChapterProgressEvent> CHAPTER_PROGRESS
            = EventFactory.createArrayBacked(ChapterProgressEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<QuestProgressEvent> QUEST_PROGRESS
            = EventFactory.createArrayBacked(QuestProgressEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );

    public static Event<TaskProgressEvent> TASK_PROGRESS
            = EventFactory.createArrayBacked(TaskProgressEvent.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    c.accept(data);
                }
            }
    );
}
