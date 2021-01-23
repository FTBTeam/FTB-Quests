package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageBase
{
	private final UUID player;
	private final int id;

	MessageClaimRewardResponse(FriendlyByteBuf buffer)
	{
		player = NetUtils.readUUID(buffer);
		id = buffer.readVarInt();
	}

	public MessageClaimRewardResponse(UUID p, int i, int t)
	{
		player = p;
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		NetUtils.writeUUID(buffer, player);
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		FTBQuests.NET_PROXY.claimReward(player, id);
	}
}