package com.feed_the_beast.ftbquests.integration.gamestages;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.reward.RewardTypes;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.ftbquests.quest.task.TaskTypes;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class GameStagesIntegration
{
	public static final TaskType GAMESTAGE_TASK = TaskTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "gamestage"), GameStageTask::new).setIcon(GuiIcons.CONTROLLER);
	public static final RewardType GAMESTAGE_REWARD = RewardTypes.register(new ResourceLocation(FTBQuests.MOD_ID, "gamestage"), GameStageReward::new).setIcon(GuiIcons.CONTROLLER);

	public void init()
	{
		MinecraftForge.EVENT_BUS.register(GameStagesIntegration.class);
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