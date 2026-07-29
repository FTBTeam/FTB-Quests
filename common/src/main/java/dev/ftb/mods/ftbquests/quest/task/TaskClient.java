package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

public interface TaskClient {
	default void onButtonClicked(Task task, Button button, boolean canClick) {
		if (canClick && task.autoSubmitOnPlayerTick() <= 0) {
			button.playClickSound();
			Play2ServerNetworking.send(new SubmitTaskMessage(task.id));
		}
	}

	@ApiStatus.NonExtendable
	default <T extends Task> Optional<T> ensureTaskType(Task task, Class<T> cls) {
		if (cls.isAssignableFrom(task.getClass())) {
			return Optional.of(cls.cast(task));
		}
		return Optional.empty();
	}

	enum Default implements TaskClient {
		INSTANCE
	}

	enum NoOp implements TaskClient {
		INSTANCE;

		@Override
		public void onButtonClicked(Task task, Button button, boolean canClick) {
		}
	}
}
