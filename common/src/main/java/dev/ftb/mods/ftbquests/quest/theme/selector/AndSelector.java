package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

import java.util.ArrayList;
import java.util.List;

public class AndSelector extends ThemeSelector {
	public final List<ThemeSelector> selectors;

	public AndSelector() {
		selectors = new ArrayList<>();
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		return selectors.stream().allMatch(selector -> selector.matches(object));
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.AND;
	}

	@Override
	public int compareTo(ThemeSelector o) {
		return o instanceof AndSelector a ?
				Integer.compare(a.selectors.size(), selectors.size()) :
				super.compareTo(o);
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