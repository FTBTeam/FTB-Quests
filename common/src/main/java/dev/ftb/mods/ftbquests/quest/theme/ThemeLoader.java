package dev.ftb.mods.ftbquests.quest.theme;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.ftb.mods.ftbquests.quest.theme.selector.*;
import dev.ftb.mods.ftbquests.util.FileUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ThemeLoader implements ResourceManagerReloadListener {
	public static final String THEME_TXT = "ftb_quests_theme.txt";

	static final Logger LOGGER = LoggerFactory.getLogger(ThemeLoader.class);
	private static final Pattern WHITESPACE_PAT = Pattern.compile("\\s");

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		loadTheme(resourceManager);
	}

	public static void loadTheme(ResourceManager resourceManager) {
		QuestTheme.setFallbackQuestObject(null);

		Map<ThemeSelector, SelectorProperties> map = new LinkedHashMap<>();

		try {
			ResourceLocation rl = FTBQuestsAPI.rl(THEME_TXT);
			for (Resource resource : resourceManager.getResourceStack(rl)) {
				try (InputStream in = resource.open()) {
					parse(map, FileUtils.read(in));
				} catch (Exception ex) {
                    LOGGER.error("Failed to load FTB Quests theme file from {}", rl, ex);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to load FTB Quests theme file", ex);
		}

		if (map.isEmpty()) {
			LOGGER.error("FTB Quests theme file is missing! Some mod has broken resource loading, inspect log for errors");
		}

		QuestTheme.reload(map);

		QuestShape.reload(findShapes(resourceManager));
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
						ThemeSelector themeSelector = ThemeSelector.parseSelector(WHITESPACE_PAT.matcher(sel1).replaceAll(""));
						if (themeSelector != null) {
							andSelector.selectors.add(themeSelector);
						}
					}

					if (!andSelector.selectors.isEmpty()) {
						ThemeSelector selector = andSelector.selectors.size() == 1 ? andSelector.selectors.getFirst() : andSelector;
						current.add(selectorPropertyMap.computeIfAbsent(selector, SelectorProperties::new));
					}
				}
			} else if (!current.isEmpty()) {
				// continuing an existing section
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

	private static List<String> findShapes(ResourceManager resourceManager) {
		// autodetect available shapes based on existence of "textures/shapes/<shape>/background.png" resources
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
