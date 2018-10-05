package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.tree.GuiQuestTree;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class MessageResetRewardResponse extends MessageToClient
{
	private int id;

	public MessageResetRewardResponse()
	{
	}

	public MessageResetRewardResponse(int i)
	{
		id = i;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(id);
	}

	@Override
	public void readData(DataIn data)
	{
		id = data.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		if (ClientQuestFile.exists())
		{
			QuestReward reward = ClientQuestFile.INSTANCE.getReward(id);

			if (reward != null)
			{
				Collection<QuestReward> collection = Collections.singleton(reward);

				for (ITeamData data : ClientQuestFile.INSTANCE.getAllData())
				{
					data.unclaimRewards(collection);
				}

				GuiQuestTree gui = ClientUtils.getCurrentGuiAs(GuiQuestTree.class);

				if (gui != null && gui.selectedQuest != null)
				{
					gui.questRight.refreshWidgets();
				}
			}
		}
	}
}