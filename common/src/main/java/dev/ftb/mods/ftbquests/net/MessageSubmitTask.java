package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class MessageSubmitTask extends MessageBase {
	private final long task;

	MessageSubmitTask(FriendlyByteBuf buffer) {
		task = buffer.readLong();
	}

	public MessageSubmitTask(long t) {
		task = t;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeLong(task);
	}

	@Override
	public void handle(NetworkManager.PacketContext context) {
		ServerPlayer player = (ServerPlayer) context.getPlayer();
		TeamData data = TeamData.get(player);

		if (data.isLocked()) {
			return;
		}

		Task t = ServerQuestFile.INSTANCE.getTask(task);

		if (t != null && data.canStartTasks(t.quest)) {
			t.submitTask(data, player);
		}
	}
}