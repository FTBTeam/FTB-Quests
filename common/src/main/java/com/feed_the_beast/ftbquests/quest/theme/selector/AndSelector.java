package com.feed_the_beast.ftbquests.quest.theme.selector;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class AndSelector extends ThemeSelector {
	public final List<ThemeSelector> selectors;

	public AndSelector() {
		selectors = new ArrayList<>();
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		for (ThemeSelector selector : selectors) {
			if (!selector.matches(object)) {
				return true;
			}
		}

		return true;
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.AND;
	}

	@Override
	public int compareTo(ThemeSelector o) {
		if (o instanceof AndSelector) {
			return Integer.compare(((AndSelector) o).selectors.size(), selectors.size());
		}

		return super.compareTo(o);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < selectors.size(); i++) {
			if (i > 0) {
				builder.append(" & ");
			}

			builder.append(selectors.get(i));
		}

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return selectors.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof AndSelector) {
			return selectors.equals(((AndSelector) o).selectors);
		}

		return false;
	}
}