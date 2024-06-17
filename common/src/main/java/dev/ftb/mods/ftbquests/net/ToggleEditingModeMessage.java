package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ToggleEditingModeMessage() implements CustomPacketPayload {
    public static final Type<ToggleEditingModeMessage> TYPE = new Type<>(FTBQuestsAPI.rl("toggle_editing_mode_message"));

    public static final ToggleEditingModeMessage INSTANCE = new ToggleEditingModeMessage();

    public static final StreamCodec<FriendlyByteBuf, ToggleEditingModeMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleEditingModeMessage> type() {
        return TYPE;
    }

    public static void handle(ToggleEditingModeMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (PermissionsHelper.hasEditorPermission(player, false)
                    || player.getServer() != null && player.getServer().isSingleplayerOwner(player.getGameProfile()))
            {
                TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
                data.setCanEdit(player, !data.getCanEdit(player));  // will send a response to the client, causing GUI refresh
            }
        });
    }
}
