package dev.ftb.mods.ftbquests.net;

import com.mojang.datafixers.util.Either;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Received on: CLIENT
 * Sent by server when a new quest object has just been created, to update all clients of its translation(s)
 *
 * @param id the quest object id
 * @param locale the current editing locale
 * @param subKey identifies which translation in the quest object this is
 * @param val the content; a string or list of string, depending on the subkey
 */
public record SyncTranslationMessageToClient(long id, String locale, TranslationKey subKey, Either<String, List<String>> val) implements CustomPacketPayload {
    public static final Type<SyncTranslationMessageToClient> TYPE = new Type<>(FTBQuestsAPI.rl("sync_translation_to_client"));

    public static StreamCodec<FriendlyByteBuf, SyncTranslationMessageToClient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncTranslationMessageToClient::id,
            ByteBufCodecs.STRING_UTF8, SyncTranslationMessageToClient::locale,
            NetworkHelper.enumStreamCodec(TranslationKey.class), SyncTranslationMessageToClient::subKey,
            ByteBufCodecs.either(ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new))), SyncTranslationMessageToClient::val,
            SyncTranslationMessageToClient::new
    );

    public static SyncTranslationMessageToClient create(QuestObjectBase obj, String locale, TranslationKey subKey, String text) {
        return new SyncTranslationMessageToClient(obj.id, locale, subKey, Either.left(text));
    }

    public static SyncTranslationMessageToClient create(QuestObjectBase obj, String locale, TranslationKey subKey, List<String> list) {
        return new SyncTranslationMessageToClient(obj.id, locale, subKey, Either.right(list));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTranslationMessageToClient message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientQuestFile file = ClientQuestFile.INSTANCE;
            if (file.isValid() && NetUtils.canEdit(context)) {
                QuestObjectBase object = file.getBase(message.id);
                if (object != null) {
                    message.val.ifLeft(str -> file.getTranslationManager().addTranslation(object, message.locale, message.subKey, str))
                            .ifRight(list -> file.getTranslationManager().addTranslation(object, message.locale, message.subKey, list));
                    object.clearCachedData();
                }
            }
        });
    }
}
