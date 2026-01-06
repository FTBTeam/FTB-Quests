package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncEditingModeMessage(UUID teamId, boolean editingMode) implements CustomPacketPayload {
	public static final Type<SyncEditingModeMessage> TYPE = new Type<>(FTBQuestsAPI.id("sync_editing_mode_message"));

	public static final StreamCodec<FriendlyByteBuf, SyncEditingModeMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SyncEditingModeMessage::teamId,
			ByteBufCodecs.BOOL, SyncEditingModeMessage::editingMode,
			SyncEditingModeMessage::new
	);

	@Override
	public Type<SyncEditingModeMessage> type() {
		return TYPE;
	}

	public static void handle(SyncEditingModeMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.syncEditingMode(message.teamId, message.editingMode));
	}
}
