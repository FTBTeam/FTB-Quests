package com.feed_the_beast.ftbquests.integration.botania;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTaskType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
public class BotaniaIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(BotaniaIntegration.class);
		GameRegistry.registerTileEntity(TileScreenCoreBotania.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core_botania"));
		GameRegistry.registerTileEntity(TileScreenPartBotania.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part_botania"));
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(new QuestTaskType(ManaTask.class, ManaTask::new).setRegistryName("botania_mana"));
	}
}