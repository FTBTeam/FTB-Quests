package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ToggleChapterPinnedMessage() implements CustomPacketPayload {
    public static final Type<ToggleChapterPinnedMessage> TYPE = new Type<>(FTBQuestsAPI.rl("toggle_chapter_pinned_message"));

    public static final ToggleChapterPinnedMessage INSTANCE = new ToggleChapterPinnedMessage();

    public static final StreamCodec<FriendlyByteBuf, ToggleChapterPinnedMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleChapterPinnedMessage> type() {
        return TYPE;
    }

    public static void handle(ToggleChapterPinnedMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            ServerQuestFile.INSTANCE.getTeamData(player).ifPresent(data -> {
                boolean newPinned = !data.isChapterPinned(player);
                data.setChapterPinned(player, newPinned);
                NetworkManager.sendToPlayer(player, new ToggleChapterPinnedResponseMessage(newPinned));
            });
        });
    }
}
