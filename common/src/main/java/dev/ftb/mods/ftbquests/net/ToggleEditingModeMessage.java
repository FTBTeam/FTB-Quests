package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ToggleEditingModeMessage() implements CustomPacketPayload {
    public static final Type<ToggleEditingModeMessage> TYPE = new Type<>(FTBQuestsAPI.id("toggle_editing_mode_message"));

    public static final ToggleEditingModeMessage INSTANCE = new ToggleEditingModeMessage();

    public static final StreamCodec<FriendlyByteBuf, ToggleEditingModeMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ToggleEditingModeMessage> type() {
        return TYPE;
    }

    public static void handle(ToggleEditingModeMessage ignoredMessage, PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (PermissionsHelper.hasEditorPermission(player, false)
                || player.level().getServer().isSingleplayerOwner(player.nameAndId()))
        {
            // will send a response to the client, causing GUI refresh
            ServerQuestFile.getInstance().getTeamData(player)
                    .ifPresent(data -> data.setCanEdit(player, !data.getCanEdit(player)));
        }
    }
}
