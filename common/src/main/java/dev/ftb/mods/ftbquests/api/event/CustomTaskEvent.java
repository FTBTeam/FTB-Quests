package dev.ftb.mods.ftbquests.api.event;

import dev.ftb.mods.ftbquests.quest.task.CustomTask;

import java.util.function.Consumer;

public interface CustomTaskEvent extends Consumer<CustomTaskEvent.Data> {
	record Data(CustomTask task) {
	}
}