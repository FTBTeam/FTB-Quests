package dev.ftb.mods.ftbquests.api.fabric;

import dev.ftb.mods.ftblibrary.util.result.DataOutcome;
import dev.ftb.mods.ftbquests.api.event.*;
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

    public static Event<ClaimRewardEvent.Pre> CLAIM_REWARD_PRE
            = EventFactory.createArrayBacked(ClaimRewardEvent.Pre.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    if (!c.test(data)) {
                        return false;
                    }
                }
                return true;
            }
    );

    public static Event<ClaimRewardEvent.GrantItem> CLAIM_REWARD_GRANT_ITEM
            = EventFactory.createArrayBacked(ClaimRewardEvent.GrantItem.class,
            callbacks -> data -> {
                for (var c : callbacks) {
                    if (!c.test(data)) {
                        return false;
                    }
                }
                return true;
            }
    );

    public static Event<OpenLootCrateEvent> OPEN_LOOT_CRATE = EventFactory.createArrayBacked(OpenLootCrateEvent.class,
            callbacks -> data -> {
                for (var event : callbacks) {
                    var outcome = event.apply(data);
                    if (outcome.isFail()) {
                        return outcome;
                    }
                }
                return DataOutcome.pass();
            }
    );
}
