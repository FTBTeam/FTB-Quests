package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.Task;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageCreatePlayerData extends MessageBase
{
	private final UUID uuid;
	private final String name;

	MessageCreatePlayerData(PacketBuffer buffer)
	{
		uuid = NetUtils.readUUID(buffer);
		name = buffer.readString();
	}

	public MessageCreatePlayerData(PlayerData data)
	{
		uuid = data.uuid;
		name = data.name;
	}

	public void write(PacketBuffer buffer)
	{
		NetUtils.writeUUID(buffer, uuid);
		buffer.writeString(name);
	}

	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.exists())
		{
			PlayerData data = new PlayerData(ClientQuestFile.INSTANCE, uuid, name);

			for (Chapter chapter : ClientQuestFile.INSTANCE.chapters)
			{
				for (Quest quest : chapter.quests)
				{
					for (Task task : quest.tasks)
					{
						data.createTaskData(task);
					}
				}
			}

			data.file.addData(data);
		}
	}
}