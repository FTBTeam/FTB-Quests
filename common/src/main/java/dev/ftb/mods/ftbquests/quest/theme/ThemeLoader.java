package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.theme.selector.*;
import dev.ftb.mods.ftbquests.util.FileUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ThemeLoader implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThemeLoader.class);

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadTheme(resourceManager);
	}

	public static void loadTheme(ResourceManager resourceManager) {
		//Map<String, ThemeProperty> propertyMap = new HashMap<>();
		//new ThemePropertyEvent(propertyMap).post();
		Map<ThemeSelector, SelectorProperties> map = new HashMap<>();

		try {
			ResourceLocation rl = FTBQuestsAPI.rl("ftb_quests_theme.txt");
			for (Resource resource : resourceManager.getResourceStack(rl)) {
				try (InputStream in = resource.open()) {
					parse(map, FileUtils.read(in));
				} catch (Exception ex) {
					LOGGER.error("Failed to load FTB Quests theme file from " + rl, ex);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to load FTB Quests theme file", ex);
		}

		if (map.isEmpty()) {
			LOGGER.error("FTB Quests theme file is missing! Some mod has broken resource loading, inspect log for errors");
		}

		QuestTheme theme = new QuestTheme();
		theme.defaults = map.remove(AllSelector.INSTANCE);

		if (theme.defaults == null) {
			theme.defaults = new SelectorProperties(AllSelector.INSTANCE);
		}

		theme.selectors.addAll(map.values());
		theme.selectors.sort(null);
		QuestTheme.instance = theme;

		LOGGER.debug("Theme:");
		LOGGER.debug("");
		LOGGER.debug("[*]");

		for (Map.Entry<String, String> entry : theme.defaults.properties.entrySet()) {
			LOGGER.debug(entry.getKey() + ": " + theme.replaceVariables(entry.getValue(), 0));
		}

		for (SelectorProperties selectorProperties : theme.selectors) {
			LOGGER.debug("");
			LOGGER.debug("[" + selectorProperties.selector + "]");

			for (Map.Entry<String, String> entry : selectorProperties.properties.entrySet()) {
				LOGGER.debug(entry.getKey() + ": " + theme.replaceVariables(entry.getValue(), 0));
			}
		}

		LinkedHashSet<String> list = new LinkedHashSet<>();
		list.add("circle");
		list.add("square");
		list.add("rsquare");

		for (String s : ThemeProperties.EXTRA_QUEST_SHAPES.get().split(",")) {
			list.add(s.trim());
		}

		QuestShape.reload(new ArrayList<>(list));
	}

	private static void parse(Map<ThemeSelector, SelectorProperties> selectorPropertyMap, List<String> lines) {
		List<SelectorProperties> current = new ArrayList<>();

		for (String line : lines) {
			line = line.trim();

			if (line.isEmpty() || line.startsWith("//")) {
				continue;
			}

			int si, ei;

			if (line.length() > 2 && ((si = line.indexOf('[')) < (ei = line.indexOf(']')))) {
				current.clear();

				for (String sel : line.substring(si + 1, ei).split("\\|")) {
					AndSelector andSelector = new AndSelector();

					for (String sel1 : sel.trim().split("&")) {
						ThemeSelector themeSelector = parse(Pattern.compile("\\s").matcher(sel1).replaceAll(""));

						if (themeSelector != null) {
							andSelector.selectors.add(themeSelector);
						}
					}

					if (!andSelector.selectors.isEmpty()) {
						ThemeSelector selector = andSelector.selectors.size() == 1 ? andSelector.selectors.get(0) : andSelector;
						current.add(selectorPropertyMap.computeIfAbsent(selector, SelectorProperties::new));
					}
				}
			} else if (!current.isEmpty()) {
				String[] s1 = line.split(":", 2);

				if (s1.length == 2) {
					String k = s1[0].trim();
					String v = s1[1].trim();

					if (!k.isEmpty() && !v.isEmpty()) {
						for (SelectorProperties selectorProperties : current) {
							selectorProperties.properties.put(k, v);
						}
					}
				}
			}
		}
	}

	@Nullable
	private static ThemeSelector parse(String sel) {
		if (sel.isEmpty()) {
			return null;
		} else if (sel.equals("*")) {
			return AllSelector.INSTANCE;
		} else if (sel.startsWith("!")) {
			ThemeSelector s = parse(sel.substring(1));
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
}
