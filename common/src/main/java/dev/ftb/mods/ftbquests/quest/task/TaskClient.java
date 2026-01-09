package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;

public interface TaskClient {
	default void onButtonClicked(Task task, Button button, boolean canClick) {
		if (canClick && task.autoSubmitOnPlayerTick() <= 0) {
			button.playClickSound();
			NetworkManager.sendToServer(new SubmitTaskMessage(task.id));
		}
	}

	enum Default implements TaskClient {
		INSTANCE;
	}

	enum NoOp implements TaskClient {
		INSTANCE;

		@Override
		public void onButtonClicked(Task task, Button button, boolean canClick) {
		}
	}
}
