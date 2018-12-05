package com.feed_the_beast.ftbquests.integration.buildcraft;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.QuestTaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class BuildCraftIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(BuildCraftIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<QuestTaskType> event)
	{
		event.getRegistry().register(FTBQuestsTasks.BUILDCRAFT_MJ = new QuestTaskType(MJTask::new).setRegistryName("buildcraft_mj").setIcon(Icon.getIcon(MJTask.EMPTY_TEXTURE.toString()).combineWith(Icon.getIcon(MJTask.FULL_TEXTURE.toString()))));
	}
}