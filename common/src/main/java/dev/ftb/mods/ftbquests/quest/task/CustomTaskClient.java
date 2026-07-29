package dev.ftb.mods.ftbquests.quest.task;

import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;

public enum CustomTaskClient implements TaskClient {
    INSTANCE;

    @Override
    public void onButtonClicked(Task task, Button button, boolean canClick) {
        ensureTaskType(task, CustomTask.class).ifPresent(customTask -> {
            if (customTask.enableButton() && canClick) {
                button.playClickSound();
                Play2ServerNetworking.send(new SubmitTaskMessage(task.getId()));
            }
        });
    }
}
