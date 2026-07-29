package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.selector.AndSelector;
import dev.ftb.mods.ftbquests.quest.theme.selector.ThemeSelector;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ThemeLoader implements ResourceManagerReloadListener {
	static final Logger LOGGER = LoggerFactory.getLogger(ThemeLoader.class);
	public static final String THEME_TXT = "ftb_quests_theme.txt";

	private static final Predicate<String> FROM_FTB_QUESTS = s -> s.equals("mod/" + FTBQuestsAPI.MOD_ID);

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadTheme(resourceManager);
	}

	public static void loadTheme(ResourceManager resourceManager) {
		Map<ThemeSelector, SelectorProperties> map = new LinkedHashMap<>();

		try {
			Identifier rl = FTBQuestsAPI.id(THEME_TXT);
			var resources = resourceManager.getResourceStack(rl).stream().sorted(ThemeLoader::resourceSorter).toList();
			for (Resource resource : resources) {
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

		// autodetect available shapes based on existence of "textures/shapes/<shape>/background.png" resources
		QuestShape.reload(findShapes(resourceManager));
	}

	private static int resourceSorter(Resource r1, Resource r2) {
		// need to ensure that "mod/ftbquests" ALWAYS comes first; everything else keeps its
		// existing relative order (mods before file resource packs), courtesy of stream.sorted()
		// being a stable sort
		boolean m1 = FROM_FTB_QUESTS.test(r1.sourcePackId());
		boolean m2 = FROM_FTB_QUESTS.test(r2.sourcePackId());
		if (m1 == m2) {
			return 0;
		}
		return m1 ? -1 : 1;
	}

	private static void parse(Map<ThemeSelector, SelectorProperties> selectorPropertyMap, List<String> lines) {
		List<SelectorProperties> current = new ArrayList<>();

		for (String line : lines) {
			line = line.trim();

			if (line.isEmpty() || line.startsWith("//")) {
				continue;
			}

			int endIndex;

			if (line.length() > 2 && line.startsWith("[") && (endIndex = line.indexOf(']')) >= 2) {
				// starting a new section
				current.clear();

				for (String sel : line.substring(1, endIndex).split("\\|")) {
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

	private static List<String> findShapes(ResourceManager resourceManager) {
		List<String> shapes = new ArrayList<>();
		var shapesMap = resourceManager.listResources("textures/shapes",
				loc -> loc.getPath().endsWith("background.png"));
		shapesMap.keySet().forEach(key -> {
			String[] parts = key.getPath().split("/");
			if (parts.length == 4 && parts[0].equals("textures") && parts[1].equals("shapes") && parts[3].equals("background.png")) {
				shapes.add(parts[2]);
			}
		});
		return shapes;
	}
}
