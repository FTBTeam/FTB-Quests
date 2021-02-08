package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author LatvianModder
 */
public class MessageMoveQuest extends MessageBase
{
	private final long id;
	private final long chapter;
	private final double x, y;

	MessageMoveQuest(FriendlyByteBuf buffer)
	{
		id = buffer.readLong();
		chapter = buffer.readLong();
		x = buffer.readDouble();
		y = buffer.readDouble();
	}

	public MessageMoveQuest(long i, long c, double _x, double _y)
	{
		id = i;
		chapter = c;
		x = _x;
		y = _y;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeLong(id);
		buffer.writeLong(chapter);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
	}

	@Override
	public void handlePacket(NetworkManager.PacketContext context)
	{
		Quest quest = ServerQuestFile.INSTANCE.getQuest(id);

		if (quest != null)
		{
			quest.moved(x, y, chapter);
			ServerQuestFile.INSTANCE.save();
			new MessageMoveQuestResponse(id, chapter, x, y).sendToAll();
		}
	}
}