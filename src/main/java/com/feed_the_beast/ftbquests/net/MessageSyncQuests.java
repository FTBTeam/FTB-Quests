package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageBase
{
	private final UUID self;
	private final QuestFile file;

	MessageSyncQuests(PacketBuffer buffer)
	{
		self = NetUtils.readUUID(buffer);
		file = new ClientQuestFile();
		file.readNetDataFull(buffer, self);
	}

	public MessageSyncQuests(UUID s, QuestFile f)
	{
		self = s;
		file = f;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		NetUtils.writeUUID(buffer, self);
		file.writeNetDataFull(buffer, self);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		((ClientQuestFile) file).load(self);
	}
}