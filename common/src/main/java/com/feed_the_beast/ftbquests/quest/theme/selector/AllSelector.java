package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class AllSelector extends ThemeSelector {
	public static final AllSelector INSTANCE = new AllSelector();

	private AllSelector() {
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		return true;
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.ALL;
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	public int hashCode() {
		return '*';
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}