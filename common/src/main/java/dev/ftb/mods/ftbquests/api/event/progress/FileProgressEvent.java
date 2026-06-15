package dev.ftb.mods.ftbquests.api.event.progress;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;

import java.util.function.Consumer;

public interface FileProgressEvent extends Consumer<FileProgressEvent.Data> {
    record Data(ProgressType type, ProgressEventData<BaseQuestFile> progressData) {
    }
}
