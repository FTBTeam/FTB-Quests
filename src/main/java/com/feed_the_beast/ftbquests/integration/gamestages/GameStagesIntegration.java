package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author LatvianModder
 */
public class GameStagesIntegration
{
	public static TaskType GAMESTAGE_TASK;
	public static RewardType GAMESTAGE_REWARD;

	public void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(TaskType.class, this::registerTasks);
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(RewardType.class, this::registerRewards);
		MinecraftForge.EVENT_BUS.register(GameStagesIntegration.class);
	}

	private void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().register(GAMESTAGE_TASK = new TaskType(GameStageTask::new).setRegistryName("gamestage").setIcon(GuiIcons.CONTROLLER));
	}

	private void registerRewards(RegistryEvent.Register<RewardType> event)
	{
		event.getRegistry().register(GAMESTAGE_REWARD = new RewardType(GameStageReward::new).setRegistryName("gamestage").setIcon(GuiIcons.CONTROLLER));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (event.getPlayer() instanceof ServerPlayer)
		{
			checkStages((ServerPlayer) event.getPlayer());
		}
	}

	@SubscribeEvent
	public static void onGameStageAdded(GameStageEvent.Added event)
	{
		if (event.getPlayer() instanceof ServerPlayer)
		{
			checkStages((ServerPlayer) event.getPlayer());
		}
	}

	@SubscribeEvent
	public static void onGameStageRemoved(GameStageEvent.Removed event)
	{
		if (event.getPlayer() instanceof ServerPlayer)
		{
			checkStages((ServerPlayer) event.getPlayer());
		}
	}

	public static void checkStages(ServerPlayer player)
	{
		PlayerData data = ServerQuestFile.INSTANCE == null || (player instanceof FakePlayer) ? null : ServerQuestFile.INSTANCE.getData(player);

		if (data != null)
		{
			for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					if (data.canStartTasks(quest))
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