package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperty;
import dev.ftb.mods.ftbquests.quest.theme.selector.AllSelector;
import dev.ftb.mods.ftbquests.quest.theme.selector.ThemeSelector;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static dev.ftb.mods.ftbquests.quest.theme.ThemeLoader.LOGGER;

public class QuestTheme {
	@Nullable
	private static QuestTheme instance;
	@Nullable
	private static QuestObjectBase fallbackQuestObject;

	// the default all-selector properties
	private final SelectorProperties defaults;
	// all selectors other than the default all-selector
	private final List<SelectorProperties> selectors;
	// simple prop-name -> value cache
	private final Map<String, Object> defaultCache;
	// per-quest-object prop-name -> value cache
	private final Map<QuestObjectPropertyKey, Object> cache;

	public QuestTheme(Map<ThemeSelector, SelectorProperties> map) {
		cache = new HashMap<>();
		defaultCache = new HashMap<>();

		var def = map.remove(AllSelector.INSTANCE);
		defaults = Objects.requireNonNullElse(def, new SelectorProperties(AllSelector.INSTANCE));

		selectors = new ArrayList<>(map.values().stream().sorted().toList());
	}

	static void setInstance(QuestTheme instance) {
		QuestTheme.instance = instance;
		instance.dumpDebugInfo();
	}

	public static QuestTheme getInstance() {
		return Objects.requireNonNull(instance);
	}

	public static @Nullable QuestObjectBase getFallbackQuestObject() {
		return fallbackQuestObject;
	}

	@Nullable
	public static QuestObjectBase setFallbackQuestObject(@Nullable QuestObjectBase fallbackQuestObject) {
		QuestObjectBase prev = QuestTheme.fallbackQuestObject;
		QuestTheme.fallbackQuestObject = fallbackQuestObject;
		return prev;
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
			object = fallbackQuestObject;
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

	private String replaceVariables(String value, int iteration) {
		if (iteration >= 30) {
			FTBQuests.LOGGER.error("quest theme parser bailed replacing value {} after 30 iterations - reference loop?", value);
			return value;
		}

		String original = value;

		for (String k : defaults.properties.keySet()) {
			value = value.replace("{{" + k + "}}", defaults.properties.get(k));
		}

		return original.equals(value) ? value : replaceVariables(value, iteration + 1);
	}

	public void dumpDebugInfo() {
		LOGGER.debug("Theme:");
		LOGGER.debug("");
		LOGGER.debug("[*]");

		for (Map.Entry<String, String> entry : defaults.properties.entrySet()) {
			LOGGER.debug("{}: {}", entry.getKey(), replaceVariables(entry.getValue(), 0));
		}

		for (SelectorProperties selectorProperties : selectors) {
			LOGGER.debug("");
			LOGGER.debug("[{}]", selectorProperties.selector);

			for (Map.Entry<String, String> entry : selectorProperties.properties.entrySet()) {
				LOGGER.debug("{}: {}", entry.getKey(), replaceVariables(entry.getValue(), 0));
			}
		}
	}

	private record QuestObjectPropertyKey(String property, long object) {
	}
}