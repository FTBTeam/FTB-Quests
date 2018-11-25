package com.feed_the_beast.ftbquests.integration.ic2;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author LatvianModder
 */
public class IC2Integration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(IC2Integration.class);
		GameRegistry.registerTileEntity(TileTaskScreenCoreIC2.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_core_ic2"));
		GameRegistry.registerTileEntity(TileTaskScreenPartIC2.class, new ResourceLocation(FTBQuests.MOD_ID, "screen_part_ic2"));
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(FTBQuestsTasks.IC2_ENERGY = new QuestTaskType(IC2EnergyTask.class, IC2EnergyTask::new).setRegistryName("ic2_energy").setIcon(Icon.getIcon(IC2EnergyTask.FULL_TEXTURE.toString()).combineWith(Icon.getIcon(IC2EnergyTask.EMPTY_TEXTURE.toString()))));
	}
}