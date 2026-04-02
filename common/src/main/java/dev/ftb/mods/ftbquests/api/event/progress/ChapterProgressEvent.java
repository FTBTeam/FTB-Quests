package dev.ftb.mods.ftbquests.api.event.progress;

import dev.ftb.mods.ftbquests.quest.Chapter;

import java.util.function.Consumer;

public interface ChapterProgressEvent extends Consumer<ChapterProgressEvent.Data> {
    record Data(ProgressType type, ProgressEventData<Chapter> progressData) {
    }
}
