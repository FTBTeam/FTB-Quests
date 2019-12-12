package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageSyncEditingMode extends MessageBase
{
	private final boolean editingMode;

	public MessageSyncEditingMode(PacketBuffer buffer)
	{
		editingMode = buffer.readBoolean();
	}

	public MessageSyncEditingMode(boolean e)
	{
		editingMode = e;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		buffer.writeBoolean(editingMode);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.self.setCanEdit(editingMode))
		{
			ClientQuestFile.INSTANCE.refreshGui();
		}
	}
}