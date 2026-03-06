package dev.ftb.mods.ftbquests.net;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;

import java.util.UUID;

public record SyncLockMessage(UUID teamId, boolean lock) implements CustomPacketPayload {
	public static final Type<SyncLockMessage> TYPE = new Type<>(FTBQuestsAPI.id("sync_lock_message"));

	public static final StreamCodec<FriendlyByteBuf, SyncLockMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SyncLockMessage::teamId,
			ByteBufCodecs.BOOL, SyncLockMessage::lock,
			SyncLockMessage::new
	);

	@Override
	public Type<SyncLockMessage> type() {
		return TYPE;
	}

	public static void handle(SyncLockMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.syncLock(message.teamId, message.lock));
	}
}
