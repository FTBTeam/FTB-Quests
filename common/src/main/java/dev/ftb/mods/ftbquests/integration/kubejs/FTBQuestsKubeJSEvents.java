package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface FTBQuestsKubeJSEvents {
	EventGroup EVENT_GROUP = EventGroup.of("FTBQuestsEvents");

	EventHandler CUSTOM_TASK = EVENT_GROUP.server("customTask", () -> CustomTaskEventJS.class).supportsExtraId().cancelable();
	EventHandler CUSTOM_REWARD = EVENT_GROUP.server("customReward", () -> CustomRewardEventJS.class).supportsExtraId().cancelable();
	EventHandler OBJECT_COMPLETED = EVENT_GROUP.server("completed", () -> QuestObjectCompletedEventJS.class).supportsExtraId();
	EventHandler OBJECT_STARTED = EVENT_GROUP.server("started", () -> QuestObjectStartedEventJS.class).supportsExtraId();
}
