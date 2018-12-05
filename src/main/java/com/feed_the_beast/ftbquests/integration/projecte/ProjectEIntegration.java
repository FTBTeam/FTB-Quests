package com.feed_the_beast.ftbquests.integration.projecte;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class ProjectEIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(ProjectEIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(FTBQuestsTasks.EMC = new QuestTaskType(EMCTask::new).setRegistryName("emc").setIcon(Icon.getIcon("projecte:items/transmute_tablet")));
	}
}