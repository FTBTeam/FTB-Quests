package com.feed_the_beast.ftbquests.integration.forestry;

import com.feed_the_beast.ftbquests.quest.task.filter.ItemFilterRegistry;

/**
 * @author LatvianModder
 */
public class ForestryIntegration
{
	public static void preInit()
	{
		ItemFilterRegistry.register(nbt -> nbt.hasKey("bee"), BeeItemFilter::new);
	}
}