package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageDeleteObject extends MessageBase
{
	private final int id;

	MessageDeleteObject(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDeleteObject(int i)
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
		if (NetUtils.canEdit(context))
		{
			ServerQuestFile.INSTANCE.deleteObject(id);
		}
	}
}