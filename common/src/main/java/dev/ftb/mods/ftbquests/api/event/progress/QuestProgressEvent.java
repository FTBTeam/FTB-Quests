package dev.ftb.mods.ftbquests.api.event.progress;

import dev.ftb.mods.ftbquests.quest.Quest;

import java.util.function.Consumer;

public interface QuestProgressEvent extends Consumer<QuestProgressEvent.Data> {
    record Data(ProgressType type, ProgressEventData<Quest> progressData) {
    }
}
