package dev.ftb.mods.ftbquests.api.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.BaseEventWithData;
import dev.ftb.mods.ftbquests.api.event.*;
import dev.ftb.mods.ftbquests.api.event.progress.ChapterProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.FileProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.QuestProgressEvent;
import dev.ftb.mods.ftbquests.api.event.progress.TaskProgressEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.ICancellableEvent;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

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

    public static class CustomReward extends BaseEventWithData<CustomRewardEvent.Data> {
        public CustomReward(CustomRewardEvent.Data data) {
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

    public static class ClaimReward {
        public static class Pre extends BaseEventWithData<ClaimRewardEvent.Pre.Data> implements ICancellableEvent {
            public Pre(ClaimRewardEvent.Pre.Data data) {
                super(data);
            }
        }

        public static class GrantItem extends BaseEventWithData<ClaimRewardEvent.GrantItem.Data> implements ICancellableEvent {
            public GrantItem(ClaimRewardEvent.GrantItem.Data data) {
                super(data);
            }
        }
    }

    public static class OpenLootCrate extends BaseEventWithData<OpenLootCrateEvent.Data> implements ICancellableEvent {
        private Component reason = Component.empty();

        public OpenLootCrate(OpenLootCrateEvent.Data data) {
            super(data);
        }

        public void fail(Component reason) {
            this.reason = reason;
            setCanceled(true);
        }

        public Component getReason() {
            return reason;
        }
    }
}
