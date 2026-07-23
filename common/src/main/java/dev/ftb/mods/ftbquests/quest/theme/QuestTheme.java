package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperty;
import dev.ftb.mods.ftbquests.quest.theme.selector.AllSelector;
import dev.ftb.mods.ftbquests.quest.theme.selector.ThemeSelector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	// known replacements (i.e. strings within "{{...}}" sequences in property values)
	private final Set<String> knownReplacements;

	private static final Pattern REPLACEMENT_PAT = Pattern.compile("\\{\\{(\\w+)}}");

	private QuestTheme(Map<ThemeSelector, SelectorProperties> map) {
		cache = new HashMap<>();
		defaultCache = new HashMap<>();

		var def = map.remove(AllSelector.INSTANCE);
		defaults = Objects.requireNonNullElse(def, new SelectorProperties(AllSelector.INSTANCE));

		selectors = new ArrayList<>(map.values().stream().sorted().toList());

		knownReplacements = new HashSet<>();
		findReplacements(defaults, knownReplacements::add);
		selectors.forEach(s -> findReplacements(s, knownReplacements::add));
	}

	private static void findReplacements(SelectorProperties properties, Consumer<String> rep) {
		properties.properties.values().forEach(val -> {
			Matcher matcher = REPLACEMENT_PAT.matcher(val);
			while (matcher.find()) {
				rep.accept(matcher.group(1));
			}
		});
	}

	static void reload(Map<ThemeSelector, SelectorProperties> map) {
		instance = new QuestTheme(map);
		instance.dumpDebugInfo();
	}

	public static QuestTheme getInstance() {
		return Objects.requireNonNull(instance);
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
			cachedValue = property.parse(replaceVariables(defaults, value));

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
						cachedValue = property.parse(replaceVariables(selectorProperties, value));

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

	public String replaceVariables(SelectorProperties selectorProperties, String value) {
		int iter = 0;
		while (iter++ < 30 && value.contains("{{") && value.contains("}}")) {
			for (String k : knownReplacements) {
				String replaced = Objects.requireNonNullElseGet(selectorProperties.properties.get(k), () -> defaults.properties.get(k));
				value = value.replace("{{" + k + "}}", replaced);
			}
		}

		return value;
	}

	public void dumpDebugInfo() {
		LOGGER.debug("Theme:");
		LOGGER.debug("");
		LOGGER.debug("[*]");

		for (Map.Entry<String, String> entry : defaults.properties.entrySet()) {
			LOGGER.debug("{}: {}", entry.getKey(), replaceVariables(defaults, entry.getValue()));
		}

		for (SelectorProperties selectorProperties : selectors) {
			LOGGER.debug("");
			LOGGER.debug("[{}]", selectorProperties.selector);

			for (Map.Entry<String, String> entry : selectorProperties.properties.entrySet()) {
				LOGGER.debug("{}: {}", entry.getKey(), replaceVariables(selectorProperties, entry.getValue()));
			}
		}
	}

	private record QuestObjectPropertyKey(String property, long object) {
	}
}