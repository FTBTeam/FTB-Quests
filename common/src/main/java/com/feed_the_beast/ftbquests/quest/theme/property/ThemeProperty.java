package com.feed_the_beast.ftbquests.quest.theme.property;

import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import com.feed_the_beast.ftbquests.quest.theme.QuestTheme;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class ThemeProperty<T> {
	public final String name;
	public final T defaultValue;

	public ThemeProperty(String n, T def) {
		name = n;
		defaultValue = def;
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