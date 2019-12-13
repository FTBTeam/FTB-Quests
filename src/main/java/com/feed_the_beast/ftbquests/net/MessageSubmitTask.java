package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.task.Task;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageSubmitTask extends MessageBase
{
	private final int task;

	MessageSubmitTask(PacketBuffer buffer)
	{
		task = buffer.readVarInt();
	}

	public MessageSubmitTask(int t)
	{
		task = t;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(task);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		ServerPlayerEntity player = context.getSender();
		PlayerData data = PlayerData.get(player);
		Task t = ServerQuestFile.INSTANCE.getTask(task);

		if (t != null && data.canStartTasks(t.quest))
		{
			data.getTaskData(t).submitTask(player);
		}
	}
}