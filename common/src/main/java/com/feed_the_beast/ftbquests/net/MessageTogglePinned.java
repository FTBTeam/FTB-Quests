package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageTogglePinned extends MessageBase
{
	private final int id;

	MessageTogglePinned(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageTogglePinned(int i)
	{
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkManager.PacketContext context)
	{
		ServerPlayer player = context.getSender();
		PlayerData data = ServerQuestFile.INSTANCE.getData(player);
		data.setQuestPinned(id, !data.isQuestPinned(id));
		new MessageTogglePinnedResponse(id).sendTo(player);
	}
}