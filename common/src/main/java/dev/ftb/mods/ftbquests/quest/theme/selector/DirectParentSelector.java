package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

/**
 * @author LatvianModder
 */
public class DirectParentSelector extends ThemeSelector {
	public final ThemeSelector parent;
	public final ThemeSelector child;

	public DirectParentSelector(ThemeSelector s, ThemeSelector c) {
		parent = s;
		child = c;
	}

	@Override
	public boolean matches(QuestObjectBase object) {
		if (!child.matches(object)) {
			return false;
		}

		QuestObjectBase o = object.getQuestFile().getBase(object.getParentID());
		return o != null && parent.matches(o);
	}

	@Override
	public ThemeSelectorType getType() {
		return ThemeSelectorType.DIRECT_PARENT;
	}

	@Override
	public String toString() {
		return parent + ">" + child;
	}

	@Override
	public int hashCode() {
		return parent.hashCode() * 31 + child.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof DirectParentSelector) {
			DirectParentSelector s = (DirectParentSelector) o;
			return parent.equals(s.parent) && child.equals(s.child);
		}

		return false;
	}
}