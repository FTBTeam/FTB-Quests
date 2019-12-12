package com.feed_the_beast.ftbquests.client;


import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class QuestFileCacheReloader implements ISelectiveResourceReloadListener
{
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestFile.INSTANCE.clearCachedData();
		}
	}
}