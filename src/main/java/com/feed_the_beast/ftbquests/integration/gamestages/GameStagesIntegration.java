package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.FTBQuestsRewards;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.FTBQuestsTasks;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * @author LatvianModder
 */
public class GameStagesIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(GameStagesIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().register(FTBQuestsTasks.GAMESTAGE = new TaskType(GameStageTask::new).setRegistryName("gamestage").setIcon(GuiIcons.CONTROLLER));
	}

	@SubscribeEvent
	public static void registerRewards(RegistryEvent.Register<RewardType> event)
	{
		event.getRegistry().register(FTBQuestsRewards.GAMESTAGE = new RewardType(GameStageReward::new).setRegistryName("gamestage").setIcon(GuiIcons.CONTROLLER));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onGameStageAdded(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.player instanceof EntityPlayerMP)
		{
			checkStages((EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public static void onGameStageAdded(GameStageEvent.Added event)
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP)
		{
			checkStages((EntityPlayerMP) event.getEntityPlayer());
		}
	}

	@SubscribeEvent
	public static void onGameStageRemoved(GameStageEvent.Removed event)
	{
		if (event.getEntityPlayer() instanceof EntityPlayerMP)
		{
			checkStages((EntityPlayerMP) event.getEntityPlayer());
		}
	}

	private static void checkStages(EntityPlayerMP player)
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
							if (task instanceof GameStageTask)
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