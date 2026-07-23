package dev.ftb.mods.ftbquests.quest.theme.selector;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import org.jetbrains.annotations.Nullable;

public abstract class ThemeSelector implements Comparable<ThemeSelector> {
	public abstract boolean matches(QuestObjectBase object);

	public abstract ThemeSelectorType getType();

	@Nullable
	public static ThemeSelector parseSelector(String sel) {
		if (sel.isEmpty()) {
			return null;
		} else if (sel.equals("*")) {
			return AllSelector.INSTANCE;
		} else if (sel.startsWith("!")) {
			ThemeSelector s = parseSelector(sel.substring(1));
			return s == null ? null : new NotSelector(s);
		} else if (QuestObjectType.NAME_MAP.map.containsKey(sel)) {
			return new TypeSelector(QuestObjectType.NAME_MAP.get(sel));
		} else if (sel.startsWith("#")) {
			String s = sel.substring(1);
			return s.isEmpty() ? null : new TagSelector(s);
		} else {
			return QuestObjectBase.parseHexId(sel).map(IDSelector::new).orElse(null);
		}
	}

	@Override
	public int compareTo(ThemeSelector o) {
		return getType().compareTo(o.getType());
	}
}