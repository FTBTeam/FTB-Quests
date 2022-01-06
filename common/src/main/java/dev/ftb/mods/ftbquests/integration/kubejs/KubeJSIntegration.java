package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.player.PlayerDataJS;
import dev.latvian.mods.kubejs.script.AttachDataEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;

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
	public void addBindings(BindingsEvent event) {
		event.add("FTBQuests", FTBQuestsKubeJSWrapper.INSTANCE);
	}

	@Override
	public void attachPlayerData(AttachDataEvent<PlayerDataJS> event) {
		event.add("ftbquests", new FTBQuestsKubeJSPlayerData(event.parent()));
	}

	public static EventResult onCustomTask(CustomTaskEvent event) {
		if (new CustomTaskEventJS(event).post(ScriptType.SERVER, "ftbquests.custom_task", event.getTask().toString())) {
			return EventResult.interruptTrue();
		}

		return EventResult.pass();
	}

	public static EventResult onCustomReward(CustomRewardEvent event) {
		if (new CustomRewardEventJS(event).post(ScriptType.SERVER, "ftbquests.custom_reward", event.getReward().toString())) {
			return EventResult.interruptTrue();
		}

		return EventResult.pass();
	}

	public static EventResult onCompleted(ObjectCompletedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			QuestObjectCompletedEventJS e = new QuestObjectCompletedEventJS(event);
			e.post(ScriptType.SERVER, "ftbquests.completed", event.getObject().getCodeString());

			for (String tag : event.getObject().getTags()) {
				e.post(ScriptType.SERVER, "ftbquests.completed." + tag);
			}
		}

		return EventResult.pass();
	}

	public static EventResult onStarted(ObjectStartedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			QuestObjectStartedEventJS e = new QuestObjectStartedEventJS(event);
			e.post(ScriptType.SERVER, "ftbquests.started", event.getObject().getCodeString());

			for (String tag : event.getObject().getTags()) {
				e.post(ScriptType.SERVER, "ftbquests.started." + tag);
			}
		}

		return EventResult.pass();
	}
}