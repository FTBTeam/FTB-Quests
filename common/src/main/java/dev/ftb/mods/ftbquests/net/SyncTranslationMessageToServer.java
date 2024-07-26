package dev.ftb.mods.ftbquests.net;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class SyncTranslationMessageToServer extends BaseC2SMessage {
    private final long id;
    private final String locale;
    private final TranslationKey subKey;
    private final Either<String, List<String>> val;
    private FriendlyByteBuf byteBuf;

    SyncTranslationMessageToServer(FriendlyByteBuf buf) {
        id = buf.readLong();
        locale = buf.readUtf();
        subKey = buf.readEnum(TranslationKey.class);
        val = buf.readEither(buf1 -> buf1.readUtf(), buf2 -> buf2.readList(buf1 -> buf1.readUtf()));
        byteBuf = buf;
    }

    public SyncTranslationMessageToServer(long id, String locale, TranslationKey subKey, Either<String, List<String>> val) {
        this.id = id;
        this.locale = locale;
        this.subKey = subKey;
        this.val = val;
    }

    public static SyncTranslationMessageToServer create(QuestObjectBase obj, String locale, TranslationKey subKey, String text) {
        return new SyncTranslationMessageToServer(obj.id, locale, subKey, subKey.validate(Either.left(text)));
    }

    public static SyncTranslationMessageToServer create(QuestObjectBase obj, String locale, TranslationKey subKey, List<String> list) {
        return new SyncTranslationMessageToServer(obj.id, locale, subKey, subKey.validate(Either.right(list)));
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.SYNC_TRANSLATION_TO_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeUtf(locale);
        buf.writeEnum(subKey);
        buf.writeEither(val, (buf1, string) -> buf1.writeUtf(string), (buf1, strings) -> buf1.writeCollection(Lists.newArrayList(), (buf2, o) -> buf2.writeUtf(String.valueOf(strings))));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (NetUtils.canEdit(context)) {
            ServerQuestFile file = ServerQuestFile.INSTANCE;
            if (file.isValid()) {
                QuestObjectBase object = file.getBase(id);
                if (object != null) {
                    val.ifLeft(str -> file.getTranslationManager().addTranslation(object, locale, subKey, str))
                            .ifRight(list -> file.getTranslationManager().addTranslation(object, locale, subKey, list));
                    NetworkHelper.sendToAll(getType(), file.server, byteBuf);
                }
            }
        }
    }
}
