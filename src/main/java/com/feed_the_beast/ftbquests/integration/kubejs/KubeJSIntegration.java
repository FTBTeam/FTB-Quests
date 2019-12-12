package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import com.feed_the_beast.ftbquests.events.ObjectCompletedEvent;
import com.feed_the_beast.ftbquests.events.TaskStartedEvent;
import dev.latvian.kubejs.documentation.DocumentationEvent;
import dev.latvian.kubejs.player.AttachPlayerDataEvent;
import dev.latvian.kubejs.script.BindingsEvent;
import dev.latvian.kubejs.script.DataType;
import dev.latvian.kubejs.script.ScriptType;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author LatvianModder
 */
public class KubeJSIntegration
{
	public void init()
	{
		MinecraftForge.EVENT_BUS.addListener(this::registerDocumentation);
		MinecraftForge.EVENT_BUS.addListener(this::registerBindings);
		MinecraftForge.EVENT_BUS.addListener(this::attachPlayerData);
		MinecraftForge.EVENT_BUS.addListener(this::onCustomTask);
		MinecraftForge.EVENT_BUS.addListener(this::onCustomReward);
		MinecraftForge.EVENT_BUS.addListener(this::onCompleted);
		MinecraftForge.EVENT_BUS.addListener(this::onTaskStarted);
	}

	private void registerDocumentation(DocumentationEvent event)
	{
		event.registerAttachedData(DataType.PLAYER, "ftbquests", FTBQuestsKubeJSPlayerData.class);

		event.registerEvent("ftbquests.custom_task", CustomTaskEventJS.class).doubleParam("id").canCancel();
		event.registerEvent("ftbquests.custom_reward", CustomRewardEventJS.class).doubleParam("id").canCancel();
		event.registerEvent("ftbquests.completed", QuestObjectCompletedEventJS.class).doubleParam("id|tag");
		event.registerEvent("ftbquests.started", TaskStartedEventJS.class).doubleParam("id|tag");
	}

	private void registerBindings(BindingsEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSWrapper());
	}

	private void attachPlayerData(AttachPlayerDataEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSPlayerData(event.getParent()));
	}

	private void onCustomTask(CustomTaskEvent event)
	{
		if (new CustomTaskEventJS(event).post(ScriptType.SERVER, "ftbquests.custom_task", event.getTask().toString()))
		{
			event.setCanceled(true);
		}
	}

	private void onCustomReward(CustomRewardEvent event)
	{
		if (new CustomRewardEventJS(event).post(ScriptType.SERVER, "ftbquests.custom_reward", event.getReward().toString()))
		{
			event.setCanceled(true);
		}
	}

	private void onCompleted(ObjectCompletedEvent event)
	{
		QuestObjectCompletedEventJS e = new QuestObjectCompletedEventJS(event);
		e.post(ScriptType.SERVER, "ftbquests.completed", event.getObject().toString());

		for (String tag : event.getObject().getTags())
		{
			e.post(ScriptType.SERVER, "ftbquests.completed." + tag);
		}
	}

	private void onTaskStarted(TaskStartedEvent event)
	{
		TaskStartedEventJS e = new TaskStartedEventJS(event);
		e.post(ScriptType.SERVER, "ftbquests.started", event.getTaskData().task.toString());

		for (String tag : event.getTaskData().task.getTags())
		{
			e.post(ScriptType.SERVER, "ftbquests.started." + tag);
		}
	}
}