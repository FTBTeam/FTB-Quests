package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.AttachedData;
import net.minecraft.world.entity.player.Player;

/**
 * @author LatvianModder
 */
public class KubeJSIntegration extends KubeJSPlugin {
	public void init() {
		CustomTaskEvent.EVENT.register(KubeJSIntegration::onCustomTask);
		CustomRewardEvent.EVENT.register(KubeJSIntegration::onCustomReward);
		ObjectCompletedEvent.GENERIC.register(KubeJSIntegration::onCompleted);
		ObjectStartedEvent.GENERIC.register(KubeJSIntegration::onStarted);
	}

	//@SubscribeEvent
	//public static void registerDocumentation(DocumentationEvent event)
	//{
	//	event.registerAttachedData(DataType.PLAYER, "ftbquests", FTBQuestsKubeJSPlayerData.class);
	//
	//	event.registerEvent("ftbquests.custom_task", CustomTaskEventJS.class).doubleParam("id").canCancel();
	//	event.registerEvent("ftbquests.custom_reward", CustomRewardEventJS.class).doubleParam("id").canCancel();
	//	event.registerEvent("ftbquests.completed", QuestObjectCompletedEventJS.class).doubleParam("id|tag");
	//	event.registerEvent("ftbquests.started", TaskStartedEventJS.class).doubleParam("id|tag");
	//}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("FTBQuests", FTBQuestsKubeJSWrapper.INSTANCE);
	}

	@Override
	public void attachPlayerData(AttachedData<Player> event) {
		event.add("ftbquests", new FTBQuestsKubeJSPlayerData(event.getParent()));
	}

	@Override
	public void registerEvents() {
		FTBQuestsKubeJSEvents.EVENT_GROUP.register();
	}

	public static EventResult onCustomTask(CustomTaskEvent event) {
		if (FTBQuestsKubeJSEvents.CUSTOM_TASK.post(event.getTask(), new CustomTaskEventJS(event))) {
			return EventResult.interruptTrue();
		}

		return EventResult.pass();
	}

	public static EventResult onCustomReward(CustomRewardEvent event) {
		if (FTBQuestsKubeJSEvents.CUSTOM_REWARD.post(event.getReward(), new CustomRewardEventJS(event))) {
			return EventResult.interruptTrue();
		}

		return EventResult.pass();
	}

	public static EventResult onCompleted(ObjectCompletedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			var kjsEvent = new QuestObjectCompletedEventJS(event);
			var object = event.getObject();

			FTBQuestsKubeJSEvents.OBJECT_COMPLETED.post(event.getObject(), kjsEvent);
			for (String tag : object.getTags()) {
				FTBQuestsKubeJSEvents.OBJECT_COMPLETED.post('#' + tag, kjsEvent);
			}
		}

		return EventResult.pass();
	}

	public static EventResult onStarted(ObjectStartedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			var kjsEvent = new QuestObjectStartedEventJS(event);
			var object = event.getObject();

			FTBQuestsKubeJSEvents.OBJECT_STARTED.post(event.getObject(), kjsEvent);
			for (String tag : object.getTags()) {
				FTBQuestsKubeJSEvents.OBJECT_STARTED.post('#' + tag, kjsEvent);
			}
		}

		return EventResult.pass();
	}
}