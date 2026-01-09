package dev.ftb.mods.ftbquests.quest.task;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;

public enum CustomTaskClient implements TaskClient {
    INSTANCE;

    @Override
    public void onButtonClicked(Task task, Button button, boolean canClick) {
        if (!(task instanceof CustomTask customTask)) {
            return;
        }

        if (customTask.enableButton() && canClick) {
            button.playClickSound();
            NetworkManager.sendToServer(new SubmitTaskMessage(customTask.id));
        }
    }
}
