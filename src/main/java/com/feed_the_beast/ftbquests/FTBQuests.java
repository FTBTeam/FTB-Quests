package com.feed_the_beast.ftbquests;

import com.feed_the_beast.ftbquests.client.FTBQuestsClient;
import com.feed_the_beast.ftbquests.integration.gamestages.GameStagesIntegration;
import com.feed_the_beast.ftbquests.integration.kubejs.KubeJSIntegration;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FTBQuests.MOD_ID)
public class FTBQuests
{
	public static final String MOD_ID = "ftbquests";
	public static final Logger LOGGER = LogManager.getLogger("FTB Quests");

	public static FTBQuests instance;

	public static FTBQuestsCommon PROXY;

	public static final ItemGroup ITEM_GROUP = new ItemGroup(FTBQuests.MOD_ID)
	{
		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack createIcon()
		{
			return new ItemStack(FTBQuestsItems.BOOK);
		}
	};

	public FTBQuests()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		//noinspection Convert2MethodRef
		PROXY = DistExecutor.runForDist(() -> () -> new FTBQuestsClient(), () -> () -> new FTBQuestsCommon());
		new FTBQuestsEventHandler().init();

		if (ModList.get().isLoaded("kubejs"))
		{
			new KubeJSIntegration().init();
		}

		if (ModList.get().isLoaded("gamestages"))
		{
			new GameStagesIntegration().init();
		}

		PROXY.init();
		TaskType.createRegistry();
		RewardType.createRegistry();
	}

	private void setup(FMLCommonSetupEvent event)
	{
		FTBQuestsNetHandler.init();
	}
}