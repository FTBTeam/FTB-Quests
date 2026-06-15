package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;

public interface TaskClient {
	default void onButtonClicked(Task task, Button button, boolean canClick) {
		if (canClick && task.autoSubmitOnPlayerTick() <= 0) {
			button.playClickSound();
			Play2ServerNetworking.send(new SubmitTaskMessage(task.id));
		}
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
