package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageTogglePinnedResponse extends MessageBase
{
	private final int id;

	public MessageTogglePinnedResponse(PacketBuffer buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageTogglePinnedResponse(int i)
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
			PlayerData data = FTBQuests.PROXY.getClientPlayerData();
			data.setQuestPinned(id, !data.isQuestPinned(id));

			ClientQuestFile.INSTANCE.questTreeGui.otherButtonsBottomPanel.refreshWidgets();

			if (ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel != null)
			{
				ClientQuestFile.INSTANCE.questTreeGui.viewQuestPanel.refreshWidgets();
			}
		}
	}
}