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
public class MessageUpdatePlayerData extends MessageBase
{
	private final UUID uuid;
	private final String name;

	MessageUpdatePlayerData(FriendlyByteBuf buffer)
	{
		uuid = NetUtils.readUUID(buffer);
		name = buffer.readUtf();
	}

	public MessageUpdatePlayerData(PlayerData data)
	{
		uuid = data.uuid;
		name = data.name;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		NetUtils.writeUUID(buffer, uuid);
		buffer.writeUtf(name);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.updatePlayerData(uuid, name);
	}
}