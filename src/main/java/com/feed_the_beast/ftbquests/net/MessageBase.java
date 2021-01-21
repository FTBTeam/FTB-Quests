package com.feed_the_beast.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public abstract class MessageBase
{
	public void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> handle(context.get()));
		context.get().setPacketHandled(true);
	}

	public abstract void write(FriendlyByteBuf buffer);

	public abstract void handle(NetworkEvent.Context context);

	public void sendToServer()
	{
		FTBQuestsNetHandler.MAIN.sendToServer(this);
	}

	public void sendToAll()
	{
		FTBQuestsNetHandler.MAIN.send(PacketDistributor.ALL.noArg(), this);
	}

	public void sendTo(ServerPlayer player)
	{
		FTBQuestsNetHandler.MAIN.send(PacketDistributor.PLAYER.with(() -> player), this);
	}

	public void sendTo(Iterable<ServerPlayer> players)
	{
		for (ServerPlayer player : players)
		{
			sendTo(player);
		}
	}
}