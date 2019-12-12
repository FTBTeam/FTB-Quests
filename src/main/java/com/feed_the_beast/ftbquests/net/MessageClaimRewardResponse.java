package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
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
		id = buffer.readInt();
	}

	public MessageClaimRewardResponse(UUID p, int i)
	{
		player = p;
		id = i;
	}

	public void write(PacketBuffer buffer)
	{
		NetUtils.writeUUID(buffer, player);
		buffer.writeInt(id);
	}

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
			reward.quest.checkRepeatableQuests(data);

			if (data == ClientQuestFile.INSTANCE.self)
			{
				GuiQuestTree treeGui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (treeGui != null)
				{
					treeGui.viewQuestPanel.refreshWidgets();
					treeGui.otherButtonsTopPanel.refreshWidgets();
				}
			}
		}
	}
}