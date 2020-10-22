package com.feed_the_beast.ftbquests.quest.theme;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestObjectType;
import com.feed_the_beast.ftbquests.quest.QuestShape;
import com.feed_the_beast.ftbquests.quest.theme.property.ThemeProperties;
import com.feed_the_beast.ftbquests.quest.theme.selector.AllSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.AndSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.IDSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.NotSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.TagSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.ThemeSelector;
import com.feed_the_beast.ftbquests.quest.theme.selector.TypeSelector;
import com.feed_the_beast.ftbquests.util.FileUtils;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ThemeLoader implements ISelectiveResourceReloadListener
{
	public static final IResourceType TYPE = new IResourceType()
	{
	};

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if (resourcePredicate.test(TYPE))
		{
			loadTheme(resourceManager);
		}
	}

	public static void loadTheme(IResourceManager resourceManager)
	{
		//Map<String, ThemeProperty> propertyMap = new HashMap<>();
		//new ThemePropertyEvent(propertyMap).post();
		Map<ThemeSelector, SelectorProperties> map = new HashMap<>();

		try
		{
			for (IResource resource : resourceManager.getAllResources(new ResourceLocation(FTBQuests.MOD_ID, "ftb_quests_theme.txt")))
			{
				try (InputStream in = resource.getInputStream())
				{
					parse(map, FileUtils.read(in));
				}
			}
		}
		catch (Exception ex)
		{
		}

		QuestTheme theme = new QuestTheme();
		theme.defaults = map.remove(AllSelector.INSTANCE);

		if (theme.defaults == null)
		{
			theme.defaults = new SelectorProperties(AllSelector.INSTANCE);
		}

		theme.selectors.addAll(map.values());
		theme.selectors.sort(null);
		QuestTheme.instance = theme;

		FTBQuests.LOGGER.debug("Theme:");
		FTBQuests.LOGGER.debug("");
		FTBQuests.LOGGER.debug("[*]");

		for (Map.Entry<String, String> entry : theme.defaults.properties.entrySet())
		{
			FTBQuests.LOGGER.debug(entry.getKey() + ": " + theme.replaceVariables(entry.getValue(), 0));
		}

		for (SelectorProperties selectorProperties : theme.selectors)
		{
			FTBQuests.LOGGER.debug("");
			FTBQuests.LOGGER.debug("[" + selectorProperties.selector + "]");

			for (Map.Entry<String, String> entry : selectorProperties.properties.entrySet())
			{
				FTBQuests.LOGGER.debug(entry.getKey() + ": " + theme.replaceVariables(entry.getValue(), 0));
			}
		}

		LinkedHashSet<String> list = new LinkedHashSet<>();
		list.add("circle");
		list.add("square");
		list.add("rsquare");

		for (String s : ThemeProperties.EXTRA_QUEST_SHAPES.get().split(","))
		{
			list.add(s.trim());
		}

		QuestShape.reload(new ArrayList<>(list));
	}

	private static void parse(Map<ThemeSelector, SelectorProperties> selectorPropertyMap, List<String> lines)
	{
		List<SelectorProperties> current = new ArrayList<>();

		for (String line : lines)
		{
			line = line.trim();

			if (line.isEmpty() || line.startsWith("//"))
			{
				continue;
			}

			int si, ei;

			if (line.length() > 2 && ((si = line.indexOf('[')) < (ei = line.indexOf(']'))))
			{
				current.clear();

				for (String sel : line.substring(si + 1, ei).split("\\|"))
				{
					AndSelector andSelector = new AndSelector();

					for (String sel1 : sel.trim().split("\\&"))
					{
						ThemeSelector themeSelector = parse(Pattern.compile("\\s").matcher(sel1).replaceAll(""));

						if (themeSelector != null)
						{
							andSelector.selectors.add(themeSelector);
						}
					}

					if (!andSelector.selectors.isEmpty())
					{
						ThemeSelector selector = andSelector.selectors.size() == 1 ? andSelector.selectors.get(0) : andSelector;
						current.add(selectorPropertyMap.computeIfAbsent(selector, SelectorProperties::new));
					}
				}
			}
			else if (!current.isEmpty())
			{
				String[] s1 = line.split(":", 2);

				if (s1.length == 2)
				{
					String k = s1[0].trim();
					String v = s1[1].trim();

					if (!k.isEmpty() && !v.isEmpty())
					{
						for (SelectorProperties selectorProperties : current)
						{
							selectorProperties.properties.put(k, v);
						}
					}
				}
			}
		}
	}

	@Nullable
	private static ThemeSelector parse(String sel)
	{
		if (sel.isEmpty())
		{
			return null;
		}
		else if (sel.equals("*"))
		{
			return AllSelector.INSTANCE;
		}
		else if (sel.startsWith("!"))
		{
			ThemeSelector s = parse(sel.substring(1));
			return s == null ? null : new NotSelector(s);
		}
		else if (QuestObjectType.NAME_MAP.map.containsKey(sel))
		{
			return new TypeSelector(QuestObjectType.NAME_MAP.get(sel));
		}
		else if (sel.startsWith("#"))
		{
			String s = sel.substring(1);
			return s.isEmpty() ? null : new TagSelector(s);
		}

		try
		{
			return new IDSelector(Long.valueOf(sel, 16).intValue());
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}