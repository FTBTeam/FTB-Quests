package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestTheme {
	public static QuestTheme instance;
	public static QuestObjectBase currentObject;

	private static class QuestObjectPropertyKey {
		private final String property;
		private final long object;

		private QuestObjectPropertyKey(String p, long o) {
			property = p;
			object = o;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(property.hashCode() * 31L + object);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof QuestObjectPropertyKey key) {
				return object == key.object && property.equals(key.property);
			}

			return false;
		}
	}

	public final List<SelectorProperties> selectors;
	private final Map<QuestObjectPropertyKey, Object> cache;
	private final Map<String, Object> defaultCache;
	public SelectorProperties defaults;

	public QuestTheme() {
		selectors = new ArrayList<>();
		cache = new HashMap<>();
		defaultCache = new HashMap<>();
	}

	public void clearCache() {
		cache.clear();
		defaultCache.clear();
	}

	public <T> T get(ThemeProperty<T> property) {
		@SuppressWarnings("unchecked") T cachedValue = (T) defaultCache.get(property.getName());

		if (cachedValue != null) {
			return cachedValue;
		}

		String value = defaults.properties.get(property.getName());

		if (value != null) {
			cachedValue = property.parse(replaceVariables(value, 0));

			if (cachedValue != null) {
				defaultCache.put(property.getName(), cachedValue);
				return cachedValue;
			}
		}

		return property.getDefaultValue();
	}

	public <T> T get(ThemeProperty<T> property, @Nullable QuestObjectBase object) {
		if (object == null) {
			object = currentObject;
		}

		if (object == null) {
			return get(property);
		}

		QuestObjectPropertyKey key = new QuestObjectPropertyKey(property.getName(), object.id);
		@SuppressWarnings("unchecked") T cachedValue = (T) cache.get(key);

		if (cachedValue != null) {
			return cachedValue;
		}

		QuestObjectBase o = object;

		do {
			for (SelectorProperties selectorProperties : selectors) {
				if (selectorProperties.selector.matches(o)) {
					String value = selectorProperties.properties.get(property.getName());

					if (value != null) {
						cachedValue = property.parse(replaceVariables(value, 0));

						if (cachedValue != null) {
							cache.put(key, cachedValue);
							return cachedValue;
						}
					}
				}
			}

			o = o.getQuestFile().getBase(o.getParentID());
		}
		while (o != null);

		return get(property);
	}

	public String replaceVariables(String value, int iteration) {
		if (iteration >= 30) {
			return value;
		}

		String original = value;

		for (String k : defaults.properties.keySet()) {
			value = value.replace("{{" + k + "}}", defaults.properties.get(k));
		}

		return original.equals(value) ? value : replaceVariables(value, iteration + 1);
	}
}