package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class SubmitTaskMessage extends BaseC2SMessage {
	private final long taskId;

	SubmitTaskMessage(FriendlyByteBuf buffer) {
		taskId = buffer.readLong();
	}

	public SubmitTaskMessage(long taskId) {
		this.taskId = taskId;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.SUBMIT_TASK;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(taskId);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();

		TeamData data = TeamData.get(player);
		if (!data.isLocked()) {
			Task task = data.getFile().getTask(taskId);
			if (task != null && data.getFile() instanceof ServerQuestFile sqf && data.canStartTasks(task.getQuest())) {
				sqf.withPlayerContext(player, () -> task.submitTask(data, player));
			}
		}
	}
}