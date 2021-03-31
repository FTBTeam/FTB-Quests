package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.TeamData;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageBase {
	private final UUID team;
	private final long task;
	private final long progress;

	public MessageUpdateTaskProgress(FriendlyByteBuf buffer) {
		team = buffer.readUUID();
		task = buffer.readLong();
		progress = buffer.readVarLong();
	}

	public MessageUpdateTaskProgress(TeamData t, long k, long p) {
		team = t.uuid;
		task = k;
		progress = p;
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