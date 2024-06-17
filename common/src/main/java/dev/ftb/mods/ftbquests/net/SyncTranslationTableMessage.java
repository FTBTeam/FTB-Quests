package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.translation.TranslationTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Received on: CLIENT
 * Sent by server to sync the translations for one locale, when player logs in or changes their client language
 *
 * @param locale the locale this table is for
 * @param table the translation table itself
 */
public record SyncTranslationTableMessage(String locale, TranslationTable table) implements CustomPacketPayload {
    public static final Type<SyncTranslationTableMessage> TYPE = new Type<>(FTBQuestsAPI.rl("sync_translation_table"));

    public static StreamCodec<FriendlyByteBuf, SyncTranslationTableMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncTranslationTableMessage::locale,
            TranslationTable.STREAM_CODEC, SyncTranslationTableMessage::table,
            SyncTranslationTableMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTranslationTableMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientQuestFile.INSTANCE.getTranslationManager().syncTableFromServer(message.locale, message.table);
            ClientQuestFile.INSTANCE.clearCachedData();
            ClientQuestFile.INSTANCE.refreshGui();
            FTBQuests.LOGGER.info("received translation table {} (with {} entries) from server", message.locale, message.table.size());
        });
    }
}
