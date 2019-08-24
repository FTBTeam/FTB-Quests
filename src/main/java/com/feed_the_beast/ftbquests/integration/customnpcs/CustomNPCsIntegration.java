package com.feed_the_beast.ftbquests.integration.customnpcs;

import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskData;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.QuestEvent;

/**
 * @author LatvianModder
 */
public class CustomNPCsIntegration
{
	public static TaskType QUEST_TASK;
	public static TaskType DIALOG_TASK;
	public static TaskType FACTION_TASK;

	public static RewardType MAIL_REWARD;
	public static RewardType FACTION_REWARD;

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
		event.getRegistry().register(MAIL_REWARD = new RewardType(NPCMailReward::new).setRegistryName("npc_mail").setIcon(GuiIcons.PLAYER));
		event.getRegistry().register(FACTION_REWARD = new RewardType(NPCFactionReward::new).setRegistryName("npc_faction").setIcon(GuiIcons.PLAYER));
	}

	@SubscribeEvent
	public static void onQuestEvent(QuestEvent event)
	{
		EntityPlayerMP player = event.player.getMCEntity();
		QuestData data = ServerQuestFile.INSTANCE.getData(player);

		if (data == null)
		{
			return;
		}

		for (NPCQuestTask task : ServerQuestFile.INSTANCE.collect(NPCQuestTask.class))
		{
			TaskData taskData = data.getTaskData(task);

			if (!taskData.isComplete() && task.npcQuest == event.quest.getId() && task.quest.canStartTasks(data))
			{
				taskData.submitTask(player);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onDialogEvent(DialogEvent.OpenEvent event)
	{
		EntityPlayerMP player = event.player.getMCEntity();
		QuestData data = ServerQuestFile.INSTANCE.getData(player);

		if (data == null)
		{
			return;
		}

		for (NPCDialogTask task : ServerQuestFile.INSTANCE.collect(NPCDialogTask.class))
		{
			TaskData taskData = data.getTaskData(task);

			if (!taskData.isComplete() && task.npcDialog == event.dialog.getId() && task.quest.canStartTasks(data))
			{
				taskData.submitTask(player);
			}
		}
	}

	@SubscribeEvent
	public static void onFactionEvent(PlayerEvent.FactionUpdateEvent event)
	{
		EntityPlayerMP player = event.player.getMCEntity();
		QuestData data = ServerQuestFile.INSTANCE.getData(player);

		if (data == null)
		{
			return;
		}

		for (NPCFactionTask task : ServerQuestFile.INSTANCE.collect(NPCFactionTask.class))
		{
			TaskData taskData = data.getTaskData(task);

			if (!taskData.isComplete() && task.npcFaction == event.faction.getId() && task.quest.canStartTasks(data))
			{
				taskData.submitTask(player);
			}
		}
	}
}