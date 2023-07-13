package dev.ftb.mods.ftbquests.integration.kubejs;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.CustomRewardEvent;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.events.ObjectStartedEvent;
import dev.ftb.mods.ftbquests.quest.task.StageTask;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.util.AttachedData;
import net.minecraft.world.entity.player.Player;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.stages.Stages;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class KubeJSIntegration extends KubeJSPlugin {
	public void init() {
		CustomTaskEvent.EVENT.register(KubeJSIntegration::onCustomTask);
		CustomRewardEvent.EVENT.register(KubeJSIntegration::onCustomReward);
		ObjectCompletedEvent.GENERIC.register(KubeJSIntegration::onCompleted);
		ObjectStartedEvent.GENERIC.register(KubeJSIntegration::onStarted);

		Stages.added(event -> {
			if (event.getPlayer() instanceof ServerPlayer sp) StageTask.checkStages(sp);
		});
		Stages.removed(event -> {
			if (event.getPlayer() instanceof ServerPlayer sp) StageTask.checkStages(sp);
		});
	}

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
		return FTBQuestsKubeJSEvents.CUSTOM_TASK.post(ScriptType.SERVER, event.getTask(), new CustomTaskEventJS(event)).arch();
	}

	public static EventResult onCustomReward(CustomRewardEvent event) {
		return FTBQuestsKubeJSEvents.CUSTOM_REWARD.post(ScriptType.SERVER, event.getReward(), new CustomRewardEventJS(event)).arch();
	}

	public static EventResult onCompleted(ObjectCompletedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			var kjsEvent = new QuestObjectCompletedEventJS(event);
			var object = event.getObject();

			FTBQuestsKubeJSEvents.OBJECT_COMPLETED.post(ScriptType.SERVER, event.getObject(), kjsEvent);
			for (String tag : object.getTags()) {
				FTBQuestsKubeJSEvents.OBJECT_COMPLETED.post(ScriptType.SERVER, '#' + tag, kjsEvent);
			}
		}

		return EventResult.pass();
	}

	public static EventResult onStarted(ObjectStartedEvent<?> event) {
		if (event.getData().file.isServerSide()) {
			var kjsEvent = new QuestObjectStartedEventJS(event);
			var object = event.getObject();

			FTBQuestsKubeJSEvents.OBJECT_STARTED.post(ScriptType.SERVER, event.getObject(), kjsEvent);
			for (String tag : object.getTags()) {
				FTBQuestsKubeJSEvents.OBJECT_STARTED.post(ScriptType.SERVER, '#' + tag, kjsEvent);
			}
		}

		return EventResult.pass();
	}
}