package com.feed_the_beast.ftbquests.events;

import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperty;

import java.util.Map;

/**
 * @author LatvianModder
 */
public class ThemePropertyEvent extends FTBQuestsEvent
{
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