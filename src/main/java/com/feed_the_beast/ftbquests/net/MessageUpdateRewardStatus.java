package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToClient;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.gui.ClientQuestFile;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author LatvianModder
 */
public class MessageUpdateRewardStatus extends MessageToClient
{
	private String reward;
	private boolean status;

	public MessageUpdateRewardStatus()
	{
	}

	public MessageUpdateRewardStatus(String r, boolean s)
	{
		reward = r;
		status = s;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeString(reward);
		data.writeBoolean(status);
	}

	@Override
	public void readData(DataIn data)
	{
		reward = data.readString();
		status = data.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage()
	{
		QuestReward qreward = ClientQuestFile.INSTANCE.getReward(reward);

		if (qreward != null && ClientQuestFile.INSTANCE.self != null)
		{
			ClientQuestFile.INSTANCE.self.setRewardStatus(qreward, status);
		}
	}
}