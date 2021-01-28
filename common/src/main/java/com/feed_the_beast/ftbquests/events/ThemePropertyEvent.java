package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperty;
import me.shedaniel.architectury.ForgeEvent;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
@ForgeEvent
public class ThemePropertyEvent
{
	public static final Event<Consumer<ThemePropertyEvent>> EVENT = EventFactory.createConsumerLoop(ThemePropertyEvent.class);
	private final Map<String, ThemeProperty> map;

	public ThemePropertyEvent(Map<String, ThemeProperty> m)
	{
		map = m;
	}

	public void register(ThemeProperty property)
	{
		map.put(property.name, property);
	}
}