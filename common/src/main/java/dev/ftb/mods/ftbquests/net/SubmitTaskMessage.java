package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record SubmitTaskMessage(long taskId) implements CustomPacketPayload {
	public static final Type<SubmitTaskMessage> TYPE = new Type<>(FTBQuestsAPI.rl("submit_task_message"));

	public static final StreamCodec<FriendlyByteBuf, SubmitTaskMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, SubmitTaskMessage::taskId,
			SubmitTaskMessage::new
	);

	@Override
	public Type<SubmitTaskMessage> type() {
		return TYPE;
	}

	public static void handle(SubmitTaskMessage message, NetworkManager.PacketContext context) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			context.queue(() -> {
				ServerQuestFile.INSTANCE.getTeamData(player).ifPresent(data -> {
					if (!data.isLocked()) {
						Task task = data.getFile().getTask(message.taskId);
						if (task != null && data.getFile() instanceof ServerQuestFile sqf && data.canStartTasks(task.getQuest())) {
							sqf.withPlayerContext(player, () -> task.submitTask(data, player));
						}
					}
				});
			});
		}
	}
}