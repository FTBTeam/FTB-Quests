package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
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
			if (o instanceof QuestObjectPropertyKey) {
				QuestObjectPropertyKey key = (QuestObjectPropertyKey) o;
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

	@SuppressWarnings("unchecked")
	public <T> T get(ThemeProperty<T> property) {
		T cachedValue = (T) defaultCache.get(property.name);

		if (cachedValue != null) {
			return cachedValue;
		}

		String value = defaults.properties.get(property.name);

		if (value != null) {
			cachedValue = property.parse(replaceVariables(value, 0));

			if (cachedValue != null) {
				defaultCache.put(property.name, cachedValue);
				return cachedValue;
			}
		}

		return property.defaultValue;
		// throw new NullPointerException("Property " + property.name + " doesn't have a default value!");
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ThemeProperty<T> property, @Nullable QuestObjectBase object) {
		if (object == null) {
			object = currentObject;
		}

		if (object == null) {
			return get(property);
		}

		QuestObjectPropertyKey key = new QuestObjectPropertyKey(property.name, object.id);
		T cachedValue = (T) cache.get(key);

		if (cachedValue != null) {
			return cachedValue;
		}

		QuestObjectBase o = object;

		do {
			for (SelectorProperties selectorProperties : selectors) {
				if (selectorProperties.selector.matches(o)) {
					String value = selectorProperties.properties.get(property.name);

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