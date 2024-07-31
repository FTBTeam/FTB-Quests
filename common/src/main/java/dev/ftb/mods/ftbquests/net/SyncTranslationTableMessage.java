package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.translation.TranslationTable;
import net.minecraft.network.FriendlyByteBuf;

public class SyncTranslationTableMessage extends BaseS2CMessage {
    private final String locale;
    private final TranslationTable table;

    SyncTranslationTableMessage(FriendlyByteBuf buffer) {
        locale = buffer.readUtf();
        table = new TranslationTable(buffer);
    }

    public SyncTranslationTableMessage(String locale, TranslationTable table) {
        this.locale = locale;
        this.table = table;
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.SYNC_TRANSLATION_TABLE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(locale);
        table.write(buffer);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ClientQuestFile.INSTANCE.getTranslationManager().syncTableFromServer(locale, table);
        ClientQuestFile.INSTANCE.clearCachedData();
        ClientQuestFile.INSTANCE.refreshGui();
        FTBQuests.LOGGER.info("received translation table {} (with {} entries) from server", locale, table.size());
    }
}
