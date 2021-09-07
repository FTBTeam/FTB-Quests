package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.networking.simple.BaseS2CMessage;
import me.shedaniel.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class UpdateTaskProgressMessage extends BaseS2CMessage {
	private final UUID team;
	private final long task;
	private final long progress;

	public UpdateTaskProgressMessage(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		task = buffer.readLong();
		progress = buffer.readVarLong();
	}

	public UpdateTaskProgressMessage(TeamData t, long k, long p) {
		team = t.uuid;
		task = k;
		progress = p;
	}

	@Override
	public MessageType getType() {
		return FTBQuestsNetHandler.UPDATE_TASK_PROGRESS;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(team);
		buffer.writeLong(task);
		buffer.writeVarLong(progress);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		FTBQuests.NET_PROXY.updateTaskProgress(team, task, progress);
	}
}