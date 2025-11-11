package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

public abstract class ThemeSelector implements Comparable<ThemeSelector> {
	public abstract boolean matches(QuestObjectBase object);

	public abstract ThemeSelectorType getType();

	@Override
	public int compareTo(ThemeSelector o) {
		return getType().compareTo(o.getType());
	}
}