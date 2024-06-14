package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Received on: SERVER
 * <br>
 * Sent by client when editing locale or auto-locale is changed in the GUI
 *
 * @param locale the player's editing locale (from config, not necessarily the same as their minecraft language)
 */
public record RequestTranslationTableMessage(String locale) implements CustomPacketPayload {
    public static final Type<RequestTranslationTableMessage> TYPE = new Type<>(FTBQuestsAPI.rl("request_translation_table"));

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
            if (ServerQuestFile.INSTANCE != null && context.getPlayer() instanceof ServerPlayer sp) {
                ServerQuestFile.INSTANCE.getTranslationManager().sendTableToPlayer(sp, message.locale);
            }
        });
    }
}
