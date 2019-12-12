package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageTogglePinned extends MessageBase
{
	private final int id;

	MessageTogglePinned(PacketBuffer buffer)
	{
		id = buffer.readInt();
	}

	public MessageTogglePinned(int i)
	{
		id = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		ServerPlayerEntity player = context.getSender();
		PlayerData data = ServerQuestFile.INSTANCE.getData(player);
		data.setQuestPinned(id, !data.isQuestPinned(id));
		new MessageTogglePinnedResponse(id).sendTo(player);
	}
}