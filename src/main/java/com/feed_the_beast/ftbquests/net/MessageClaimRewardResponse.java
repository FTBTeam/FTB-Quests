package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.chest.GuiQuestChest;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageClaimRewardResponse extends MessageToClient
{
	private int uid;

	public MessageClaimRewardResponse()
	{
	}

	public MessageClaimRewardResponse(int id)
	{
		uid = id;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(uid);
	}

	@Override
	public void readData(DataIn data)
	{
		uid = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.existsWithTeam())
		{
			ClientQuestFile.INSTANCE.rewards.add(uid);

			QuestReward reward = ClientQuestFile.INSTANCE.getReward(uid);

			if (reward != null)
			{
				reward.quest.checkRepeatableQuests(ClientQuestFile.INSTANCE.self, ClientUtils.MC.player.getUniqueID());
			}

			GuiQuestTree treeGui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

			if (treeGui != null)
			{
				treeGui.questLeft.refreshWidgets();
				treeGui.questRight.refreshWidgets();
				treeGui.otherButtons.refreshWidgets();
				treeGui.otherButtons.alignWidgets();
			}
			else
			{
				GuiQuestChest guiChest = ClientUtils.getCurrentGuiAs(GuiQuestChest.class);

				if (guiChest != null)
				{
					guiChest.updateRewards();
				}
			}
		}
	}
}