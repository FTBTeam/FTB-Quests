package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.quest.theme.selector.ThemeSelector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class SelectorProperties implements Comparable<SelectorProperties> {
	public final ThemeSelector selector;
	public final Map<String, String> properties;

	public SelectorProperties(ThemeSelector s) {
		selector = s;
		properties = new LinkedHashMap<>();
	}

	@Override
	public int compareTo(SelectorProperties o) {
		return selector.compareTo(o.selector);
	}
}