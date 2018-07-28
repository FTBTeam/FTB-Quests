package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestList;
import com.feed_the_beast.ftbquests.quest.rewards.QuestReward;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageClaimReward extends MessageToServer
{
	private short reward;

	public MessageClaimReward()
	{
	}

	public MessageClaimReward(short r)
	{
		reward = r;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsNetHandler.GENERAL;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeShort(reward);
	}

	@Override
	public void readData(DataIn data)
	{
		reward = data.readShort();
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		QuestReward r = ServerQuestList.INSTANCE.getReward(reward);

		if (r != null)
		{
			new MessageUpdateRewardStatus(reward, FTBQuestsTeamData.get(Universe.get().getPlayer(player).team).claimReward(player, r)).sendTo(player);
		}
	}
}