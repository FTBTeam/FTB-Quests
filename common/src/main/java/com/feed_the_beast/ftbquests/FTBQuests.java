package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.client.FTBQuestsNetClient;
import com.feed_the_beast.ftbquests.integration.kubejs.KubeJSIntegration;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.reward.RewardTypes;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.registry.CreativeTabs;
import me.shedaniel.architectury.utils.EnvExecutor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBQuests
{
	public static final String MOD_ID = "ftbquests";
	public static final Logger LOGGER = LogManager.getLogger("FTB Quests");

	public static FTBQuests instance;

	public static FTBQuestsCommon PROXY;
	public static FTBQuestsNetCommon NET_PROXY;

	public static final CreativeModeTab ITEM_GROUP = CreativeTabs.create(new ResourceLocation(FTBQuests.MOD_ID, FTBQuests.MOD_ID), () ->
			new ItemStack(FTBQuestsItems.BOOK.get()));

	public FTBQuests()
	{
		TaskTypes.init();
		RewardTypes.init();
		FTBQuestsNetHandler.init();
		PROXY = EnvExecutor.getEnvSpecific(() -> FTBQuestsClient::new, () -> FTBQuestsCommon::new);
		NET_PROXY = EnvExecutor.getEnvSpecific(() -> FTBQuestsNetClient::new, () -> FTBQuestsNetCommon::new);
		new FTBQuestsEventHandler().init();

		if (Platform.isModLoaded("kubejs"))
		{
			KubeJSIntegration.init();
		}

		PROXY.init();
	}
}