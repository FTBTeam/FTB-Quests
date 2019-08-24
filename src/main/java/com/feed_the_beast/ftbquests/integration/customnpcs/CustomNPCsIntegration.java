package com.feed_the_beast.ftbquests.integration.customnpcs;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class CustomNPCsIntegration
{
	public static TaskType QUEST_TASK;
	public static TaskType DIALOG_TASK;
	public static TaskType FACTION_TASK;

	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(CustomNPCsIntegration.class);
	}

	@SubscribeEvent
	public static void registerTasks(RegistryEvent.Register<TaskType> event)
	{
		event.getRegistry().register(QUEST_TASK = new TaskType(NPCQuestTask::new).setRegistryName("npc_quest").setIcon(GuiIcons.PLAYER));
		event.getRegistry().register(DIALOG_TASK = new TaskType(NPCDialogTask::new).setRegistryName("npc_dialog").setIcon(GuiIcons.PLAYER));
		event.getRegistry().register(FACTION_TASK = new TaskType(NPCFactionTask::new).setRegistryName("npc_faction").setIcon(GuiIcons.PLAYER));
	}

	@SubscribeEvent
	public static void registerRewards(RegistryEvent.Register<RewardType> event)
	{
		//event.getRegistry().register(GAMESTAGE = new RewardType(GameStageReward::new).setRegistryName("gamestage").setIcon(GuiIcons.CONTROLLER));
	}
}