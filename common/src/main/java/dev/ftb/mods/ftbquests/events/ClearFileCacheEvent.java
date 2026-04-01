package dev.ftb.mods.ftbquests.events;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;

import java.util.function.Consumer;

public interface ClearFileCacheEvent extends Consumer<ClearFileCacheEvent.Data> {
	record Data(BaseQuestFile file) {
	}
}