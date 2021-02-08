package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageUpdateTaskProgress extends MessageBase
{
	private final UUID player;
	private final long task;
	private final long progress;

	public MessageUpdateTaskProgress(FriendlyByteBuf buffer)
	{
		player = NetUtils.readUUID(buffer);
		task = buffer.readLong();
		progress = buffer.readVarLong();
	}

	public MessageUpdateTaskProgress(PlayerData t, long k, long p)
	{
		player = t.uuid;
		task = k;
		progress = p;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		NetUtils.writeUUID(buffer, player);
		buffer.writeLong(task);
		buffer.writeVarLong(progress);
	}

	@Override
	public void handlePacket(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.updateTaskProgress(player, task, progress);
	}
}