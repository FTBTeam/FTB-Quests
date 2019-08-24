package com.feed_the_beast.ftbquests.integration.projecte;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import moze_intel.projecte.PECore;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class ProjectEIntegration
{
	public static TaskType EMC_TASK;

	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(ProjectEIntegration.class);
		FMLInterModComms.sendMessage(PECore.MODID, "nbtwhitelist", new ItemStack(FTBQuestsItems.SCREEN));
		FMLInterModComms.sendMessage(PECore.MODID, "nbtwhitelist", new ItemStack(FTBQuestsItems.PROGRESS_SCREEN));
		FMLInterModComms.sendMessage(PECore.MODID, "nbtwhitelist", new ItemStack(FTBQuestsItems.LOOTCRATE));
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().register(EMC_TASK = new TaskType(EMCTask::new).setRegistryName("emc").setIcon(Icon.getIcon("projecte:items/transmute_tablet")));
	}
}