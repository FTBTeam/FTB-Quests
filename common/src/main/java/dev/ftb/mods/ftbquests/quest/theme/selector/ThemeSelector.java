package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;

import java.util.Optional;

public abstract class ThemeSelector implements Comparable<ThemeSelector> {
	public static Optional<ThemeSelector> parseSelector(String sel) {
		if (sel.isEmpty()) {
			return Optional.empty();
		} else if (sel.equals("*")) {
			return Optional.of(AllSelector.INSTANCE);
		} else if (sel.startsWith("!")) {
			return parseSelector(sel.substring(1)).map(NotSelector::new);
		} else if (QuestObjectType.NAME_MAP.map.containsKey(sel)) {
			return Optional.of(new TypeSelector(QuestObjectType.NAME_MAP.get(sel)));
		} else if (sel.startsWith("#")) {
			String s = sel.substring(1);
			return s.isEmpty() ? Optional.empty() : Optional.of(new TagSelector(s));
		} else {
			return QuestObjectBase.parseHexId(sel).map(IDSelector::new);
		}
	}

	public abstract boolean matches(QuestObjectBase object);

	public abstract ThemeSelectorType getType();

	@Override
	public int compareTo(ThemeSelector o) {
		return getType().compareTo(o.getType());
	}
}