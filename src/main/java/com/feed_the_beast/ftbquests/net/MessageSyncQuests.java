package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageSyncQuests extends MessageBase
{
	private final UUID self;
	private final QuestFile file;

	MessageSyncQuests(FriendlyByteBuf buffer)
	{
		self = NetUtils.readUUID(buffer);
		file = FTBQuests.PROXY.createClientQuestFile();
		file.readNetDataFull(buffer, self);
	}

	public MessageSyncQuests(UUID s, QuestFile f)
	{
		self = s;
		file = f;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		NetUtils.writeUUID(buffer, self);
		file.writeNetDataFull(buffer, self);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		file.load(self);
	}
}