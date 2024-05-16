package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ToggleChapterPinnedResponseMessage(boolean pinned) implements CustomPacketPayload {
    public static final Type<ToggleChapterPinnedResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("toggle_chapter_pinned_response_message"));

    public static final StreamCodec<FriendlyByteBuf, ToggleChapterPinnedResponseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ToggleChapterPinnedResponseMessage::pinned,
            ToggleChapterPinnedResponseMessage::new
    );

    @Override
    public Type<ToggleChapterPinnedResponseMessage> type() {
        return TYPE;
    }

    public static void handle(ToggleChapterPinnedResponseMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBQuestsNetClient.toggleChapterPinned(message.pinned));
    }
}
