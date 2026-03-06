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

public record UpdateTaskProgressMessage(UUID teamId, long task, long progress) implements CustomPacketPayload {
	public static final Type<UpdateTaskProgressMessage> TYPE = new Type<>(FTBQuestsAPI.id("update_task_progress_message"));

	public static final StreamCodec<FriendlyByteBuf, UpdateTaskProgressMessage> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, UpdateTaskProgressMessage::teamId,
			ByteBufCodecs.VAR_LONG, UpdateTaskProgressMessage::task,
			ByteBufCodecs.VAR_LONG, UpdateTaskProgressMessage::progress,
			UpdateTaskProgressMessage::new
	);

	@Override
	public Type<UpdateTaskProgressMessage> type() {
		return TYPE;
	}

	public static void handle(UpdateTaskProgressMessage message, NetworkManager.PacketContext context) {
		context.queue(() -> FTBQuestsNetClient.updateTaskProgress(message.teamId, message.task, message.progress));
	}
}
