package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.net.snm.BaseC2SPacket;
import dev.ftb.mods.ftblibrary.net.snm.PacketID;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class SubmitTaskPacket extends BaseC2SPacket {
	private final long task;

	SubmitTaskPacket(FriendlyByteBuf buffer) {
		task = buffer.readLong();
	}

	public SubmitTaskPacket(long t) {
		task = t;
	}

	@Override
	public PacketID getId() {
		return FTBQuestsNetHandler.SUBMIT_TASK;
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

		Task t = data.file.getTask(task);

		if (t != null && data.canStartTasks(t.quest)) {
			((ServerQuestFile) data.file).currentPlayer = player;
			t.submitTask(data, player);
			((ServerQuestFile) data.file).currentPlayer = null;
		}
	}
}