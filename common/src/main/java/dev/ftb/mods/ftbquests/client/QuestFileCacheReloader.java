package dev.ftb.mods.ftbquests.client;


import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class QuestFileCacheReloader implements ResourceManagerReloadListener {
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		if (ClientQuestFile.exists()) {
			ClientQuestFile.INSTANCE.clearCachedData();
		}
	}
}