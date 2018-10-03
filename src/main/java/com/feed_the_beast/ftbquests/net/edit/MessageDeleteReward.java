package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.ITeamData;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.QuestReward;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class MessageDeleteReward extends MessageToServer
{
	private int id;

	public MessageDeleteReward()
	{
	}

	public MessageDeleteReward(int i)
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
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestReward reward = ServerQuestFile.INSTANCE.getReward(id);

			if (reward != null)
			{
				Collection<QuestReward> collection = Collections.singleton(reward);

				for (ITeamData data : ServerQuestFile.INSTANCE.getAllData())
				{
					data.unclaimRewards(collection);
				}

				reward.quest.rewards.remove(reward);
				ServerQuestFile.INSTANCE.allRewards.remove(id);
				ServerQuestFile.INSTANCE.save();
				new MessageDeleteRewardResponse(id).sendToAll();
			}
		}
	}
}