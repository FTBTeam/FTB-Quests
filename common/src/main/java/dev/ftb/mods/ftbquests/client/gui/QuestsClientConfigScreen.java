package dev.ftb.mods.ftbquests.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.RequestTranslationTableMessage;

import java.util.ArrayList;
import java.util.List;

public class QuestsClientConfigScreen extends EditConfigScreen {
    private final String prevLocale;
    private final String prevFallback;
    private final boolean pause;

    public QuestsClientConfigScreen(EditableConfigGroup group, boolean pause) {
        super(group);

        this.pause = pause;
        this.prevLocale = ClientQuestFile.getInstance().getLocale();
        this.prevFallback = ClientQuestFile.getInstance().getFallbackLocale();

        setAutoclose(true);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ftbquests.gui.preferences").withStyle(ChatFormatting.UNDERLINE);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return pause;
    }

    @Override
    protected void doAccept() {
        super.doAccept();

        if (ClientQuestFile.exists()) {
            ClientQuestFile file = ClientQuestFile.getInstance();
            List<CustomPacketPayload> toSend = new ArrayList<>();
            if (!prevLocale.equals(file.getLocale())) {
                toSend.add(new RequestTranslationTableMessage(file.getLocale()));
            }
            if (!prevFallback.equals(file.getFallbackLocale())) {
                toSend.add(new RequestTranslationTableMessage(file.getFallbackLocale()));
            }
            if (!toSend.isEmpty()) {
                toSend.forEach(NetworkManager::sendToServer);
                file.clearCachedData();
            }
        }
    }
}
