package com.feed_the_beast.ftbquests.client;


import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class QuestFileCacheReloader implements ISelectiveResourceReloadListener
{
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestFile.INSTANCE.clearCachedData();
		}
	}
}