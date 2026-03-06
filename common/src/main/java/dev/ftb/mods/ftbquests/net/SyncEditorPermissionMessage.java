package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;

public record SyncEditorPermissionMessage(boolean hasPermission) implements CustomPacketPayload {
    public static final Type<SyncEditorPermissionMessage> TYPE = new Type<>(FTBQuestsAPI.id("sync_editor_permission_message"));

    public static final StreamCodec<FriendlyByteBuf, SyncEditorPermissionMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncEditorPermissionMessage::hasPermission,
            SyncEditorPermissionMessage::new
    );

    public static SyncEditorPermissionMessage forPlayer(ServerPlayer p) {
        return new SyncEditorPermissionMessage(PermissionsHelper.hasEditorPermission(p, false));
    }

    @Override
    public Type<SyncEditorPermissionMessage> type() {
        return TYPE;
    }

    public static void handle(SyncEditorPermissionMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> FTBQuestsNetClient.setEditorPermission(message.hasPermission));
    }
}
