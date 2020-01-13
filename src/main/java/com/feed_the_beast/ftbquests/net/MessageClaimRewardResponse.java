package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.quests.GuiQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.util.NetUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageBase
{
	private final UUID player;
	private final int id;

	MessageClaimRewardResponse(PacketBuffer buffer)
	{
		player = NetUtils.readUUID(buffer);
		id = buffer.readVarInt();
	}

	public MessageClaimRewardResponse(UUID p, int i, int t)
	{
		player = p;
		id = i;
	}

	@Override
	public void write(PacketBuffer buffer)
	{
		NetUtils.writeUUID(buffer, player);
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (ClientQuestFile.exists())
		{
			Reward reward = ClientQuestFile.INSTANCE.getReward(id);

			if (reward == null)
			{
				return;
			}

			PlayerData data = ClientQuestFile.INSTANCE.getData(player);
			data.setRewardClaimed(reward.id, true);

			if (data == ClientQuestFile.INSTANCE.self)
			{
				GuiQuests treeGui = ClientUtils.getCurrentGuiAs(GuiQuests.class);

				if (treeGui != null)
				{
					treeGui.viewQuestPanel.refreshWidgets();
					treeGui.otherButtonsTopPanel.refreshWidgets();
				}
			}
		}
	}
}