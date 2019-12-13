package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.quest.QuestObjectBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDeleteObjectResponse extends MessageBase
{
	private final int id;

	MessageDeleteObjectResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDeleteObjectResponse(int i)
	{
		id = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.exists())
		{
			QuestObjectBase object = ClientQuestFile.INSTANCE.getBase(id);

			if (object != null)
			{
				object.deleteChildren();
				object.deleteSelf();
				ClientQuestFile.INSTANCE.refreshIDMap();
				object.editedFromGUI();
				FTBQuestsJEIHelper.refresh(object);
			}
		}
	}
}