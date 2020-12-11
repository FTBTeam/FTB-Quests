package com.feed_the_beast.ftbquests.integration.reskillable;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;

import codersafterdark.reskillable.api.event.LevelUpEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ReskillableItegration {
	
	public static TaskType RESKILLABLE_TASK;
	
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(ReskillableItegration.class);
	}
	
	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().register(RESKILLABLE_TASK = new TaskType(ReskillableTask::new).setRegistryName("reskillable").setIcon(Icon.getIcon(ReskillableTask.RESKILLABLE_TEXTURE.toString())));
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			checkSkills((EntityPlayerMP) event.player);
		}
	}
	
	@SubscribeEvent
	public static void onLevelUp(LevelUpEvent.Post event) 
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP)
		{
			checkSkills((EntityPlayerMP) event.getEntityPlayer());
		}
	}
	
	private static void checkSkills(EntityPlayerMP player)
	{
		QuestData data = ServerQuestFile.INSTANCE == null ? null : ServerQuestFile.INSTANCE.getData(player);

		if (data != null)
		{
			for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (quest.canStartTasks(data))
					{
						for (Task task : quest.tasks)
						{
							if (task instanceof ReskillableTask)
							{
								data.getTaskData(task).submitTask(player);
							}
						}
					}
				}
			}
		}
	}
}

