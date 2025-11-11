package dev.ftb.mods.ftbquests.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperty;

import java.util.Map;
import java.util.function.Consumer;

public class ThemePropertyEvent {
	public static final Event<Consumer<ThemePropertyEvent>> EVENT = EventFactory.createConsumerLoop(ThemePropertyEvent.class);
	private final Map<String, ThemeProperty<?>> map;

	public ThemePropertyEvent(Map<String, ThemeProperty<?>> m) {
		map = m;
	}

	public void register(ThemeProperty<?> property) {
		map.put(property.getName(), property);
	}
}