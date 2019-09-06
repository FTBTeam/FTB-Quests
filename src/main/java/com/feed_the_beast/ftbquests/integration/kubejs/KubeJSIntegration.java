package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import dev.latvian.kubejs.documentation.DocumentationEvent;
import dev.latvian.kubejs.event.EventsJS;
import dev.latvian.kubejs.player.AttachPlayerDataEvent;
import dev.latvian.kubejs.script.BindingsEvent;
import dev.latvian.kubejs.script.DataType;
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
		event.registerAttachedData(DataType.PLAYER, "ftbquests", FTBQuestsKubeJSPlayerData.class);

		event.registerDoubleEvent("ftbquests.custom_task", "id", CustomTaskEventJS.class, true);
		event.registerDoubleEvent("ftbquests.custom_reward", "id", CustomRewardEventJS.class, true);
		event.registerDoubleEvent("ftbquests.completed", "id", QuestObjectCompletedEventJS.class);
	}

	@SubscribeEvent
	public static void registerBindings(BindingsEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSWrapper());
	}

	@SubscribeEvent
	public static void attachPlayerData(AttachPlayerDataEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSPlayerData(event.getParent()));
	}

	@SubscribeEvent
	public static void onCustomTask(CustomTaskEvent event)
	{
		if (EventsJS.postDouble("ftbquests.custom_task", event.getTask().getEventID(), new CustomTaskEventJS(event)))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onCustomReward(CustomRewardEvent event)
	{
		if (EventsJS.postDouble("ftbquests.custom_reward", event.getReward().getEventID(), new CustomRewardEventJS(event)))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onCompleted(ObjectCompletedEvent event)
	{
		EventsJS.postDouble("ftbquests.completed", event.getObject().getEventID(), new QuestObjectCompletedEventJS(event));
	}
}