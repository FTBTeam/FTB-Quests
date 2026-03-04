package dev.ftb.mods.ftbquests.quest.theme;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.quest.theme.selector.AndSelector;
import dev.ftb.mods.ftbquests.quest.theme.selector.ThemeSelector;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThemeLoader implements ResourceManagerReloadListener {
	static final Logger LOGGER = LoggerFactory.getLogger(ThemeLoader.class);
	public static final String THEME_TXT = "ftb_quests_theme.txt";

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadTheme(resourceManager);
	}

	public static void loadTheme(ResourceManager resourceManager) {
		Map<ThemeSelector, SelectorProperties> map = new HashMap<>();

		try {
			Identifier rl = FTBQuestsAPI.id(THEME_TXT);
			for (Resource resource : resourceManager.getResourceStack(rl)) {
				try (InputStream in = resource.open()) {
					parse(map, IOUtils.readLines(in, StandardCharsets.UTF_8));
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to load/parse FTB Quests theme file {} from resources: {}", THEME_TXT, ex);
		}

		if (map.isEmpty()) {
			LOGGER.error("FTB Quests theme file is missing! Some mod has broken resource loading, inspect log for errors");
		}

		QuestTheme.setInstance(new QuestTheme(map));

		LinkedHashSet<String> shapes = new LinkedHashSet<>(List.of("circle", "square", "rsquare"));
		for (String s : ThemeProperties.EXTRA_QUEST_SHAPES.get().split(",\\s*")) {
			shapes.add(s.trim());
		}
		shapes.add("none");
		QuestShape.reload(new ArrayList<>(shapes));
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
				// starting a new section
				current.clear();

				for (String sel : line.substring(si + 1, ei).split("\\|")) {
					AndSelector andSelector = new AndSelector();
					for (String sel1 : sel.trim().split("&")) {
						ThemeSelector.parseSelector(StringUtils.deleteWhitespace(sel1))
								.ifPresent(andSelector.selectors::add);
					}
					if (!andSelector.selectors.isEmpty()) {
						ThemeSelector selector = andSelector.selectors.size() == 1 ? andSelector.selectors.getFirst() : andSelector;
						current.add(selectorPropertyMap.computeIfAbsent(selector, SelectorProperties::new));
					}
				}
			} else if (!current.isEmpty()) {
				// a key/value pair within a section
				String[] s1 = line.split(":", 2);

				if (s1.length == 2) {
					String key = s1[0].trim();
					String val = s1[1].trim();

					if (!key.isEmpty() && !val.isEmpty()) {
						for (SelectorProperties selectorProperties : current) {
							selectorProperties.properties.put(key, val);
						}
					}
				}
			}
		}
	}
}
