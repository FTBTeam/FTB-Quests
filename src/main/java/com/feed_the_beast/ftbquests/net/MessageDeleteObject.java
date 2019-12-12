package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDeleteObject extends MessageBase
{
	private final int id;

	MessageDeleteObject(PacketBuffer buffer)
	{
		id = buffer.readInt();
	}

	public MessageDeleteObject(int i)
	{
		id = i;
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeInt(id);
	}

	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			ServerQuestFile.INSTANCE.deleteObject(id);
		}
	}
}