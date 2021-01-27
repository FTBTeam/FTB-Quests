package com.feed_the_beast.ftbquests.client;


import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * @author LatvianModder
 */
public class QuestFileCacheReloader implements ResourceManagerReloadListener
{
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
		if (ClientQuestFile.exists())
		{
			ClientQuestFile.INSTANCE.clearCachedData();
		}
	}
}