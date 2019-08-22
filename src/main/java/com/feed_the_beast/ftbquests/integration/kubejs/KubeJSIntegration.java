package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import dev.latvian.kubejs.KubeJSEventRegistryEvent;
import dev.latvian.kubejs.events.EventsJS;
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
	public static void registerEvents(KubeJSEventRegistryEvent event)
	{
		event.register("ftbquests.custom_task.<id>", CustomTaskEventJS.class);
		event.register("ftbquests.custom_reward.<id>", CustomRewardEventJS.class);
	}

	@SubscribeEvent
	public static void onCustomTask(CustomTaskEvent event)
	{
		if (!event.getTask().getQuestFile().isClient())
		{
			CustomTaskEventJS e = new CustomTaskEventJS();
			EventsJS.INSTANCE.post("ftbquests.custom_task." + event.getTask(), e);

			if (e.check != null)
			{
				event.getTask().check = new CheckWrapper(e.check);
			}
		}
	}

	@SubscribeEvent
	public static void onCustomReward(CustomRewardEvent event)
	{
		if (!event.getReward().getQuestFile().isClient())
		{
			EventsJS.INSTANCE.post("ftbquests.custom_reward." + event.getReward(), new CustomRewardEventJS(event.getPlayer()));
		}
	}
}