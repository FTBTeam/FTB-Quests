package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class IDSelector extends ThemeSelector {
	public final int id;

	public IDSelector(int i) {
		id = i;
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		return object.id == id;
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.ID;
	}

	@Override
	public String toString() {
		return QuestObjectBase.getCodeString(id);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof IDSelector) {
			return id == ((IDSelector) o).id;
		}

		return false;
	}
}