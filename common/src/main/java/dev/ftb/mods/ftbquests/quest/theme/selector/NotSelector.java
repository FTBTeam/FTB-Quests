package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class NotSelector extends ThemeSelector {
	public final ThemeSelector selector;

	public NotSelector(ThemeSelector s) {
		selector = s;
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		return !selector.matches(object);
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.NOT;
	}

	@Override
	public int compareTo(ThemeSelector o) {
		if (o instanceof NotSelector) {
			return ((NotSelector) o).selector.compareTo(selector);
		}

		return super.compareTo(o);
	}

	@Override
	public String toString() {
		return "!" + selector;
	}

	@Override
	public int hashCode() {
		return -selector.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof NotSelector) {
			return selector.equals(((NotSelector) o).selector);
		}

		return false;
	}
}