package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

/**
 * Received on: SERVER
 * <br>
 * Sent by client when editing locale or auto-locale is changed in the GUI
 *
 * @param locale the player's editing locale (from config, not necessarily the same as their minecraft language)
 */
public record RequestTranslationTableMessage(String locale) implements CustomPacketPayload {
    public static final Type<RequestTranslationTableMessage> TYPE = new Type<>(FTBQuestsAPI.id("request_translation_table"));

    public static final StreamCodec<FriendlyByteBuf, RequestTranslationTableMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RequestTranslationTableMessage::locale,
            RequestTranslationTableMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestTranslationTableMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (ServerQuestFile.getInstance() != null && context.getPlayer() instanceof ServerPlayer sp) {
                ServerQuestFile.getInstance().getTranslationManager().sendTableToPlayer(sp, message.locale);
            }
        });
    }
}
