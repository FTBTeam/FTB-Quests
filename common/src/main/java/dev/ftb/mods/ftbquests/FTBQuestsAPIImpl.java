package dev.ftb.mods.ftbquests;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.api.ItemFilterAdapter;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.Objects;

public enum FTBQuestsAPIImpl implements FTBQuestsAPI.API {
    INSTANCE;

    @Override
    public BaseQuestFile getQuestFile(boolean isClient) {
        return isClient ?
                Objects.requireNonNull(FTBQuestsClient.getClientQuestFile()) :
                ServerQuestFile.getInstance();
    }

    @Override
    public void registerFilterAdapter(ItemFilterAdapter adapter) {
        ItemMatchingSystem.INSTANCE.registerFilterAdapter(adapter);
    }
}
