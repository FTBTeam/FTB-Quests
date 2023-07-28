package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class UpdateTaskProgressMessage extends BaseS2CMessage {
	private final UUID teamId;
	private final long task;
	private final long progress;

	public UpdateTaskProgressMessage(FriendlyByteBuf buffer) {
		teamId = buffer.readUUID();
		task = buffer.readLong();
		progress = buffer.readVarLong();
	}

	public UpdateTaskProgressMessage(TeamData teamData, long task, long progress) {
		teamId = teamData.getTeamId();
		this.task = task;
		this.progress = progress;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.UPDATE_TASK_PROGRESS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(teamId);
		buffer.writeLong(task);
		buffer.writeVarLong(progress);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuestsNetClient.updateTaskProgress(teamId, task, progress);
	}
}