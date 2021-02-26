package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public abstract class ThemeSelector implements Comparable<ThemeSelector> {
	public abstract boolean matches(QuestObjectBase object);

	public abstract ThemeSelectorType getType();

	@Override
	public int compareTo(ThemeSelector o) {
		return getType().compareTo(o.getType());
	}
}