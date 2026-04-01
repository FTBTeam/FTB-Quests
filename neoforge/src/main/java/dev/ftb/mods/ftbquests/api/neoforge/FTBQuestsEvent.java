package dev.ftb.mods.ftbquests.api.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.BaseEventWithData;
import dev.ftb.mods.ftbquests.events.*;
import dev.ftb.mods.ftbquests.events.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.events.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.events.progress.QuestProgressEvent;
import dev.ftb.mods.ftbquests.events.progress.TaskProgressEvent;

public class FTBQuestsEvent {
    public static class ClearFileCache extends BaseEventWithData<ClearFileCacheEvent.Data> {
        public ClearFileCache(ClearFileCacheEvent.Data data) {
            super(data);
        }
    }

    public static class CustomTask extends BaseEventWithData<CustomTaskEvent.Data> {
        public CustomTask(CustomTaskEvent.Data data) {
            super(data);
        }
    }

    public static class FileProgress extends BaseEventWithData<FileProgressEvent.Data> {
        public FileProgress(FileProgressEvent.Data data) {
            super(data);
        }
    }

    public static class ChapterProgress extends BaseEventWithData<ChapterProgressEvent.Data> {
        public ChapterProgress(ChapterProgressEvent.Data data) {
            super(data);
        }
    }

    public static class QuestProgress extends BaseEventWithData<QuestProgressEvent.Data> {
        public QuestProgress(QuestProgressEvent.Data data) {
            super(data);
        }
    }

    public static class TaskProgress extends BaseEventWithData<TaskProgressEvent.Data> {
        public TaskProgress(TaskProgressEvent.Data data) {
            super(data);
        }
    }
}
