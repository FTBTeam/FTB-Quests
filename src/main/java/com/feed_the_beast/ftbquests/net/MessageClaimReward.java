package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import com.feed_the_beast.ftbquests.util.FTBQuestsTeamData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author LatvianModder
 */
public class MessageClaimReward extends MessageToServer
{
	private int uid;

	public MessageClaimReward()
	{
	}

	public MessageClaimReward(int id)
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
	public void onMessage(EntityPlayerMP player)
	{
		QuestReward reward = ServerQuestFile.INSTANCE.allRewards.get(uid);

		if (reward != null)
		{
			FTBQuestsTeamData teamData = FTBQuestsTeamData.get(Universe.get().getPlayer(player).team);

			if (reward.quest.isComplete(teamData))
			{
				teamData.claimReward(player, reward);
			}
		}
	}
}