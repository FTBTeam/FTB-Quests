package dev.ftb.mods.ftbquests.client.gui;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;

public class QuestsClientConfigScreen extends EditConfigScreen {
    private final String prevLocale;
    private final boolean pause;

    public QuestsClientConfigScreen(ConfigGroup group, boolean pause) {
        super(group);

        this.pause = pause;
        this.prevLocale = ClientQuestFile.INSTANCE.getLocale();

        setAutoclose(true);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return pause;
    }

    @Override
    protected void doAccept() {
        super.doAccept();

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file != null && !prevLocale.equals(file.getLocale())) {
            NetworkManager.sendToServer(new RequestTranslationTableMessage(file.getLocale()));
            file.clearCachedData();
        }
    }
}
