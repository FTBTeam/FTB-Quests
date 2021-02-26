package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class TagSelector extends ThemeSelector {
	public final String tag;

	public TagSelector(String t) {
		tag = t;
	}

	@Override
	public boolean matches(QuestObjectBase quest) {
		return quest.hasTag(tag);
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.TAG;
	}

	@Override
	public String toString() {
		return "#" + tag;
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof TagSelector) {
			return tag.equals(((TagSelector) o).tag);
		}

		return false;
	}
}