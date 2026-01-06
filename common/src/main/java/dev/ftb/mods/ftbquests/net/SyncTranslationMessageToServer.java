package dev.ftb.mods.ftbquests.net;

import com.mojang.datafixers.util.Either;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Received on: SERVER
 * Sent by client when any translatable text is updated via GUI for an existing object
 *
 * @param id the quest object id
 * @param locale the current editing locale
 * @param subKey identifies which translation in the quest object this is
 * @param val the content; a string or list of string, depending on the subkey
 */
public record SyncTranslationMessageToServer(long id, String locale, TranslationKey subKey, Either<String, List<String>> val) implements CustomPacketPayload {
    public static final Type<SyncTranslationMessageToServer> TYPE = new Type<>(FTBQuestsAPI.id("sync_translation_to_server"));

    public static StreamCodec<FriendlyByteBuf, SyncTranslationMessageToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SyncTranslationMessageToServer::id,
            ByteBufCodecs.STRING_UTF8, SyncTranslationMessageToServer::locale,
            NetworkHelper.enumStreamCodec(TranslationKey.class), SyncTranslationMessageToServer::subKey,
            ByteBufCodecs.either(ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new))), SyncTranslationMessageToServer::val,
            SyncTranslationMessageToServer::new
    );

    public static SyncTranslationMessageToServer create(QuestObjectBase obj, String locale, TranslationKey subKey, String text) {
        return new SyncTranslationMessageToServer(obj.id, locale, subKey, subKey.validate(Either.left(text)));
    }

    public static SyncTranslationMessageToServer create(QuestObjectBase obj, String locale, TranslationKey subKey, List<String> list) {
        return new SyncTranslationMessageToServer(obj.id, locale, subKey, subKey.validate(Either.right(list)));
    }

    public SyncTranslationMessageToClient createResponse() {
        return new SyncTranslationMessageToClient(id, locale, subKey, val);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTranslationMessageToServer message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (NetUtils.canEdit(context)) {
                ServerQuestFile file = ServerQuestFile.INSTANCE;
                if (file.isValid()) {
                    QuestObjectBase object = file.getBase(message.id);
                    if (object != null) {
                        message.val.ifLeft(str -> file.getTranslationManager().addTranslation(object, message.locale, message.subKey, str))
                                .ifRight(list -> file.getTranslationManager().addTranslation(object, message.locale, message.subKey, list));
                        NetworkHelper.sendToAll(file.server, message.createResponse());
                    }
                }
            }
        });
    }
}
