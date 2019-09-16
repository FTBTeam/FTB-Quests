package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
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

		event.registerEvent("ftbquests.custom_task", CustomTaskEventJS.class).doubleParam("id").canCancel();
		event.registerEvent("ftbquests.custom_reward", CustomRewardEventJS.class).doubleParam("id").canCancel();
		event.registerEvent("ftbquests.completed", QuestObjectCompletedEventJS.class).doubleParam("id|tag");
		event.registerEvent("ftbquests.started", TaskStartedEventJS.class).doubleParam("id|tag");
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
		if (EventsJS.postDouble("ftbquests.custom_task", event.getTask().toString(), new CustomTaskEventJS(event)))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onCustomReward(CustomRewardEvent event)
	{
		if (EventsJS.postDouble("ftbquests.custom_reward", event.getReward().toString(), new CustomRewardEventJS(event)))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onCompleted(ObjectCompletedEvent event)
	{
		QuestObjectCompletedEventJS e = new QuestObjectCompletedEventJS(event);
		EventsJS.postDouble("ftbquests.completed", event.getObject().toString(), e);

		for (String tag : event.getObject().getTags())
		{
			EventsJS.post("ftbquests.completed." + tag, e);
		}
	}

	@SubscribeEvent
	public static void onTaskStarted(TaskStartedEvent event)
	{
		TaskStartedEventJS e = new TaskStartedEventJS(event);
		EventsJS.postDouble("ftbquests.started", event.getTaskData().task.toString(), e);

		for (String tag : event.getTaskData().task.getTags())
		{
			EventsJS.post("ftbquests.started." + tag, e);
		}
	}
}