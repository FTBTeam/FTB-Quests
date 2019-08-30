package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.quest.task.CustomTask;
import dev.latvian.kubejs.documentation.AttachedDataType;
import dev.latvian.kubejs.documentation.DocumentationEvent;
import dev.latvian.kubejs.event.EventsJS;
import dev.latvian.kubejs.player.PlayerDataCreatedEvent;
import dev.latvian.kubejs.script.BindingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class KubeJSIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(KubeJSIntegration.class);
	}

	@SubscribeEvent
	public static void registerDocumentation(DocumentationEvent event)
	{
		event.registerAttachedData(AttachedDataType.PLAYER, "ftbquests", FTBQuestsKubeJSPlayerData.class);

		event.registerDoubleEvent("ftbquests.custom_task", "id", CustomTaskEventJS.class);
		event.registerDoubleEvent("ftbquests.custom_reward", "id", CustomRewardEventJS.class);
		event.registerDoubleEvent("ftbquests.completed", "id", QuestObjectCompletedEventJS.class);
	}

	@SubscribeEvent
	public static void registerBindings(BindingsEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSWrapper());
	}

	@SubscribeEvent
	public static void onPlayerDataCreated(PlayerDataCreatedEvent event)
	{
		event.setData("ftbquests", new FTBQuestsKubeJSPlayerData(event.getPlayerData()));
	}

	@SubscribeEvent
	public static void onCustomTask(CustomTaskEvent event)
	{
		CustomTask c = event.getTask();

		if (!c.getQuestFile().isClient())
		{
			CustomTaskEventJS e = new CustomTaskEventJS(event.getTask());

			if (EventsJS.postDouble("ftbquests.custom_task", e.task.toString(), e) && e.check != null)
			{
				c.check = new CheckWrapper(e.check);
				c.checkTimer = e.checkTimer;
				c.enableButton = e.enableButton;
				c.maxProgress = e.maxProgress;
			}
		}
	}

	@SubscribeEvent
	public static void onCustomReward(CustomRewardEvent e)
	{
		if (!e.getReward().getQuestFile().isClient())
		{
			EventsJS.postDouble("ftbquests.custom_reward", e.getReward().toString(), new CustomRewardEventJS(e.getPlayer(), e.getReward(), e.getNotify()));
		}
	}

	@SubscribeEvent
	public static void onCompleted(ObjectCompletedEvent e)
	{
		if (!e.getObject().getQuestFile().isClient())
		{
			EventsJS.postDouble("ftbquests.completed", e.getObject().toString(), new QuestObjectCompletedEventJS(e.getData(), e.getObject(), e.getNotifiedPlayers()));
		}
	}
}