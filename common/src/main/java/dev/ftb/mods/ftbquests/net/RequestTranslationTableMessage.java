package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class RequestTranslationTableMessage extends BaseC2SMessage {
    private final String locale;

    RequestTranslationTableMessage(FriendlyByteBuf buf) {
        this.locale = buf.readUtf();
    }

    public RequestTranslationTableMessage(String locale) {
        this.locale = locale;
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.REQUEST_TRANSLATION_TABLE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(locale);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (ServerQuestFile.INSTANCE != null && context.getPlayer() instanceof ServerPlayer sp) {
            ServerQuestFile.INSTANCE.getTranslationManager().sendTableToPlayer(sp, this.locale);
        }
    }
}
