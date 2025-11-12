package dev.ftb.mods.ftbquests.quest.theme.property;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import org.jetbrains.annotations.Nullable;

public abstract class ThemeProperty<T> {
	private final String name;
	private final T defaultValue;

	public ThemeProperty(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	@Nullable
	public abstract T parse(String string);

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof ThemeProperty && name.equals(o.toString());
	}

	public T get(@Nullable QuestObjectBase object) {
		return QuestTheme.instance.get(this, object);
	}

	public T get() {
		return get(null);
	}
}