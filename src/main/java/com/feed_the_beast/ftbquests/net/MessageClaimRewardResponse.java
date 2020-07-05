package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageToClient
{
	private short team;
	private UUID player;
	private int id;

	public MessageClaimRewardResponse()
	{
	}

	public MessageClaimRewardResponse(short t, UUID p, int i)
	{
		team = t;
		player = p;
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(team);
		data.writeUUID(player);
		data.writeInt(id);
	}

	@Override
	public void readData(DataIn data)
	{
		team = data.readShort();
		player = data.readUUID();
		id = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			Reward reward = ClientQuestFile.INSTANCE.getReward(id);

			if (reward == null)
			{
				return;
			}

			QuestData data = ClientQuestFile.INSTANCE.getData(team);

			if (data == null)
			{
				return;
			}

			data.setRewardClaimed(player, reward);
			reward.quest.checkRepeatableQuests(data, player);

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